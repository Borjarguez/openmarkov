/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.constraint;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;

/*****
 * This class validates if a link satisfies the conditions to be a revelation
 * arc.
 *
 * @author ckonig
 *
 */
public class RevelationArcValidator {
	/**********
	 * Checks if the link satisfies the conditions to have the condition of
	 * revelation arc
	 *
	 * @param link
	 * @return <code>true</code> if a link restriction can be applied to the
	 *         link.
	 */
	public static boolean validate(Link<Node> link) {

		Node node1 = link.getNode1();
		Node node2 = link.getNode2();
		ProbNet net = node1.getProbNet();
		if (!net.hasConstraint(NoRevelationArc.class)) {

			if ((node1.getNodeType() == NodeType.CHANCE || node1.getNodeType() == NodeType.DECISION) && (
					node2.getNodeType() == NodeType.CHANCE
			)) {

				return true;
			}
		}
		return false;
	}

}
