/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.algorithm.hillclimbing;

import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.BaseLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.algorithm.hillclimbing.util.HillClimbingEditProposal;
import org.openmarkov.learning.algorithm.scoreAndSearch.ScoreAndSearchAlgorithm;
import org.openmarkov.learning.algorithm.scoreAndSearch.metric.Metric;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmType;
import org.openmarkov.learning.core.util.LearningEditMotivation;
import org.openmarkov.learning.core.util.LearningEditProposal;
import org.openmarkov.learning.core.util.ScoreEditMotivation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the basic structure of the classic hill climber
 * algorithm.
 *
 * @author joliva
 * @author manuel
 * @author fjdiez
 * @author ibermejo
 * @version 1.1
 * @since OpenMarkov 1.0
 */
@LearningAlgorithmType(name = "Hill climbing") public class HillClimbingAlgorithm extends ScoreAndSearchAlgorithm {

	/**
	 * Metric used as heuristic
	 */
	protected Metric metric;

	/**
	 * Net we are generating edits for
	 */
	protected ProbNet probNet;

	/**
	 * List with the best edits that have been not been done by the
	 * algorithm because they violate the ModelNetworkConstraint
	 */
	protected List<PNEdit> lastBestEdits;

	// Constructor

	/**
	 * @param probNet
	 * @param caseDatabase
	 * @param alpha        double parameter alpha
	 *                     gives the best operation in each iteration of the algorithm.
	 * @param metric       for the learning.
	 **/
	public HillClimbingAlgorithm(ProbNet probNet, CaseDatabase caseDatabase, Double alpha, Metric metric) {
		super(probNet, caseDatabase, metric, alpha);
		this.probNet = probNet;
		this.metric = metric;
		this.lastBestEdits = new ArrayList<PNEdit>();

		resetHistory();
	}

	@Override public LearningEditMotivation getMotivation(PNEdit edit) {
		// TODO: Review. Perhaps score should come from inference?
		return new ScoreEditMotivation(metric.getScore(edit));
	}

	/**
	 * This method returns the best edit (and its associated score)
	 * that can be done to the network that is being learnt.
	 *
	 * @param onlyAllowedEdits  If this parameter is true, only those edits
	 *                          that do not provoke a ConstraintViolationException are returned
	 * @param onlyPositiveEdits If this parameter is true, only those
	 *                          edits with a positive associated score are returned.
	 * @return <code>LearningEditProposal</code> with the best edit and its score.
	 */
	public LearningEditProposal getBestEdit(boolean onlyAllowedEdits, boolean onlyPositiveEdits) {
		resetHistory();
		return getNextEdit(onlyAllowedEdits, onlyPositiveEdits);
	}

	/**
	 * This method returns the next best edit (and its associated score)
	 * that can be done to the network that is being learnt.
	 *
	 * @param onlyAllowedEdits  If this parameter is true, only those edits
	 *                          that do not provoke a ConstraintViolationException are returned
	 * @param onlyPositiveEdits If this parameter is true, only those
	 *                          edits with a positive associated score are returned.
	 * @return <code>LearningEditProposal</code> with the best edit and its score.
	 */
	public LearningEditProposal getNextEdit(boolean onlyAllowedEdits, boolean onlyPositiveEdits) {
		LearningEditProposal bestEdit = getOptimalEdit(probNet, onlyAllowedEdits, onlyPositiveEdits);
		while (bestEdit != null && isBlocked(bestEdit.getEdit())) {
			bestEdit = getOptimalEdit(probNet, onlyAllowedEdits, onlyPositiveEdits);
		}
		return bestEdit;
	}

	protected void resetHistory() {
		lastBestEdits.clear();
	}

	/**
	 * Store last best edit
	 *
	 * @param edit
	 */
	protected void markEditAsConsidered(BaseLinkEdit edit) {
		lastBestEdits.add(edit);
	}

	/**
	 * @param edit
	 * @return true iff it is an edit already considered
	 */
	protected boolean isEditAlreadyConsidered(BaseLinkEdit edit) {
		return lastBestEdits.contains(edit);
	}

	/**
	 * Method to obtain the edit with the highest associated score.
	 *
	 * @param learnedNet net to learn.
	 * @return <code>PNEdit</code> edit with the highest associated score.
	 */
	private LearningEditProposal getOptimalEdit(ProbNet learnedNet, boolean onlyAllowedEdits,
			boolean onlyPositiveEdits) {
		double bestPartialScore = Double.NEGATIVE_INFINITY;
		LearningEditProposal bestEditProposal = null;
		BaseLinkEdit bestEdit = null;
		for (Variable head : learnedNet.getVariables()) {
			for (Variable tail : learnedNet.getVariables()) {
				if (!head.equals(tail)) {
					Node headNode = learnedNet.getNode(head);
					Node tailNode = learnedNet.getNode(tail);

					if (!headNode.isParent(tailNode)) {
						AddLinkEdit addLinkEdit = new AddLinkEdit(learnedNet, tail, head, true);
						double addScore = metric.getScore(addLinkEdit);
						/*
						 * Check whether the score is the best to the moment and
						 * whether this edit has not been already considered
						 */
						if ((addScore > bestPartialScore) && !isEditAlreadyConsidered(addLinkEdit)) {
							if ((!onlyAllowedEdits || isAllowed(addLinkEdit)) && (!onlyPositiveEdits || addScore > 0)
									&& !isBlocked(addLinkEdit)) {
								bestEdit = addLinkEdit;
								bestPartialScore = addScore;
							}
						}
					} else {
						RemoveLinkEdit removeLinkEdit = new RemoveLinkEdit(learnedNet, tail, head, true);
						double removeScore = metric.getScore(removeLinkEdit);
						/*
						 * Check whether the score is the best to the moment and
						 * whether this edit has not been already considered
						 */
						if ((removeScore > bestPartialScore) && !isEditAlreadyConsidered(removeLinkEdit)) {
							if ((!onlyAllowedEdits || isAllowed(removeLinkEdit)) && (
									!onlyPositiveEdits || removeScore > 0
							) && !isBlocked(removeLinkEdit)) {
								bestEdit = removeLinkEdit;
								bestPartialScore = removeScore;
							}
						}

						InvertLinkEdit invertLinkEdit = new InvertLinkEdit(learnedNet, tail, head, true);
						double invertScore = metric.getScore(invertLinkEdit);
						/*
						 * Check whether the score is the best to the moment and
						 * whether this edit has not been already considered
						 */
						if ((invertScore > bestPartialScore) && !isEditAlreadyConsidered(invertLinkEdit)) {
							if ((!onlyAllowedEdits || isAllowed(invertLinkEdit)) && (
									!onlyPositiveEdits || invertScore > 0
							) && !isBlocked(invertLinkEdit)) {
								bestEdit = invertLinkEdit;
								bestPartialScore = invertScore;
							}
						}
					}
				}
			}
		}
		if (bestEdit != null) {
			bestEditProposal = new HillClimbingEditProposal(bestEdit, bestPartialScore);
			markEditAsConsidered(bestEdit);
		}
		return bestEditProposal;
	}

}
