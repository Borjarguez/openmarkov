/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.treeadd;

import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.gui.dialog.common.OkCancelApplyUndoRedoHorizontalDialog;

import java.awt.*;

/**
 * @author myebra
 */
@SuppressWarnings("serial") public class ChangeIntervalDialog extends OkCancelApplyUndoRedoHorizontalDialog {
	private ChangeIntervalPanel changeIntervalPanel;
	/**
	 * Dialog string resource.
	 */
	// private StringResource dialogStringResource;
	// private TreeADDPotential treeADDParent;
	private TreeADDBranch treeBranch;

	public ChangeIntervalDialog(Window owner, TreeADDBranch treeBranch) {
		super(owner);
		this.treeBranch = treeBranch;
		// add(checkBoxPanel, BorderLayout.NORTH );
		initialize();
		setLocationRelativeTo(owner);
		setMinimumSize(new Dimension(200, 200));
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
		/*
		 * dialogStringResource =
		 * StringResourceLoader.getUniqueInstance().getBundleDialogs();
		 * messageStringResource =
		 * StringResourceLoader.getUniqueInstance().getBundleMessages();
		 * setTitle(dialogStringResource
		 * .getValuesInAString("NodePotentialDialog.Title.Label"));
		 */
		getComponentsPanel().setLayout(new BorderLayout(5, 5));
		// getComponentsPanel().add( getJPanelChangeInterval(),
		// BorderLayout.CENTER );
		getComponentsPanel().add(getChangeIntervalPanel(), BorderLayout.CENTER);
	}

	protected ChangeIntervalPanel getChangeIntervalPanel() {
		if (changeIntervalPanel == null) {
			setName("NodeDomainValuesTablePanel");
			// changeIntervalPanel = new ChangeIntervalPanel(columnNames,
			// treeADDParent);
			changeIntervalPanel = new ChangeIntervalPanel(treeBranch);
			// changeIntervalPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
			changeIntervalPanel.setLayout(new FlowLayout());
			changeIntervalPanel.setName("jPanelChangeInterval");
		}
		return changeIntervalPanel;
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
