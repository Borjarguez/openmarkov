/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.core.util;

import java.math.BigDecimal;

public class ScoreEditMotivation extends LearningEditMotivation {
	private double score;

	public ScoreEditMotivation(double score) {
		this.score = score;
	}

	public int compareTo(LearningEditMotivation edit) {
		int comparison = 0;
		if (score > ((ScoreEditMotivation) edit).score) {
			comparison = 1;
		} else if (score < ((ScoreEditMotivation) edit).score) {
			comparison = -1;
		}
		return comparison;
	}

	@Override public String toString() {
		return new BigDecimal(score).setScale(2, BigDecimal.ROUND_FLOOR).toString();
	}

	/**
	 * Returns the score.
	 *
	 * @return the score.
	 */
	public double getScore() {
		return score;
	}
}
