/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial") public class SplitIntervalPanel extends JPanel {

	private JTextField limitField;
	private JLabel limit;
	private JRadioButton belongsToLeft;
	private JRadioButton belongsToRight;

	public SplitIntervalPanel() {

		initialize();
		repaint();

	}

	public void initialize() {
		limitField = new JTextField(4);
		limitField.setBounds(133, 24, 38, 20);
		setLayout(null);
		limit = new JLabel("Threshold value: ");
		limit.setBounds(27, 27, 83, 14);
		add(limit);
		Component verticalStrut = Box.createVerticalStrut(10);
		verticalStrut.setBounds(0, 0, 0, 0);
		add(verticalStrut);
		add(limitField);
		Component verticalStrut_1 = Box.createVerticalStrut(10);
		verticalStrut_1.setBounds(0, 0, 0, 0);
		add(verticalStrut_1);
		JSeparator separator = new JSeparator();
		separator.setBounds(0, 0, 0, 0);
		add(separator);
		//multiple exclusion group
		ButtonGroup group = new ButtonGroup();

		belongsToLeft = new JRadioButton();
		belongsToLeft.setBounds(21, 59, 153, 23);
		belongsToLeft.setHorizontalAlignment(SwingConstants.LEFT);
		//belongsToLeft.setText("Belongs to left ]( : ");
		belongsToLeft.setText("Included in first interval ]( ");

		belongsToLeft.setSelected(true);
		group.add(belongsToLeft);
		add(belongsToLeft);

		belongsToRight = new JRadioButton();
		belongsToRight.setBounds(21, 85, 167, 23);
		//belongsToRight.setText("Belong to right )[ : " );
		belongsToRight.setText("Included in second interval )[ ");
		belongsToRight.setVisible(true);
		group.add(belongsToRight);
		add(belongsToRight);

	}

	public JRadioButton belongsToLeft() {
		return belongsToLeft;
	}

	public JRadioButton belongsToRight() {
		return belongsToRight;
	}

	public JTextField getLimit() {
		return limitField;
	}

}
