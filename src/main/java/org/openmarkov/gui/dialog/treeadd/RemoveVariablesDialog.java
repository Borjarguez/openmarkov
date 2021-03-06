/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.gui.dialog.common.OkCancelApplyUndoRedoHorizontalDialog;

import javax.swing.*;
import java.awt.*;

/**
 * @author myebra
 */
@SuppressWarnings("serial") public class RemoveVariablesDialog extends OkCancelApplyUndoRedoHorizontalDialog {

	private RemoveVariablesCheckBoxPanel removeVariablesCheckBoxPanel;
	private TreeADDBranch treeADDBranch;
	private TreeADDPotential parentTreeADD;

	public RemoveVariablesDialog(Window owner, TreeADDBranch treeADDBranch, TreeADDPotential parentTreeADD) {
		super(owner);
		this.treeADDBranch = treeADDBranch;
		this.parentTreeADD = parentTreeADD;
		initialize();
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(100, 100));
		setResizable(true);
		pack();
	}

	private void initialize() {
		configureComponentsPanel();
		pack();
	}

	/**
	 * Sets up the panel where all components, except the buttons of the buttons
	 * panel, will be appear.
	 */
	private void configureComponentsPanel() {
		/*dialogStringResource =
				StringResourceLoader.getUniqueInstance().getBundleDialogs();
		messageStringResource =
				StringResourceLoader.getUniqueInstance().getBundleMessages();
		setTitle(dialogStringResource
				.getValuesInAString("NodePotentialDialog.Title.Label"));*/
		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		getComponentsPanel().add(getJPanelVariables(), BorderLayout.CENTER);

	}

	protected JPanel getJPanelVariables() {

		if (removeVariablesCheckBoxPanel == null) {
			removeVariablesCheckBoxPanel = new RemoveVariablesCheckBoxPanel(treeADDBranch, parentTreeADD);
			//removeVariablesCheckBoxPanel.setLayout( new FlowLayout() );
			removeVariablesCheckBoxPanel.setName("jPanelRemoveVariables");

		}
		return removeVariablesCheckBoxPanel;

	}

	public int requestValues() {

		setVisible(true);

		return selectedButton;
	}

	/**
	 * This method carries out the actions when the user press the Ok button
	 * before hide the dialog.
	 *
	 * @return true if the dialog box can be closed.
	 */
	protected boolean doOkClickBeforeHide() {
		return true;
	}

	/**
	 * This method carries out the actions when the user press the Cancel button
	 * before hide the dialog.
	 */
	protected void doCancelClickBeforeHide() {

	}
}
