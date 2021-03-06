/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.gui.constraint.AbsorbParentsValidator;
import org.openmarkov.gui.constraint.AbsorbNodeValidator;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.localize.MenuLocalizer;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;
import org.openmarkov.gui.window.edition.EditorPanel;
import org.openmarkov.gui.window.edition.NetworkPanel;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * This class implements a contextual menu that is displayed when the user
 * clicks on a node.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.2 asaez - Add options for expanding and contracting nodes, setting
 * and deleting findings and policies.
 */
public class NodeContextualMenu extends ContextualMenu {
	/**
	 * Static field for serializable class.
	 */
	private static final long serialVersionUID = 8556550568033250304L;
	/**
	 * Object that represents the item 'Cut'.
	 */
	private JMenuItem cutMenuItem = null;
	/**
	 * Object that represents the item 'Copy'.
	 */
	private JMenuItem copyMenuItem = null;
	/**
	 * Object that represents the item 'Remove'.
	 */
	private JMenuItem removeMenuItem = null;
	/**
     * Object that represents the item 'AbsorbNode'.
     */
    private JMenuItem absorbNodeMenuItem = null;
	/**
     * Object that represents the item 'AbsorbParents'.
     */
    private JMenuItem absorbParentsMenuItem = null;
	/**
	 * Object that represents the item 'Properties'.
	 */
	private JMenuItem propertiesMenuItem = null;
	private JMenuItem relationMenuItem;
	/**
	 * Object that represents the item 'ImposePolicy'.
	 */
	private JMenuItem imposePolicyMenuItem = null;
	/**
	 * Object that represents the item 'EditPolicy'.
	 */
	private JMenuItem editPolicyMenuItem = null;
	/**
	 * Object that represents the item 'RemovePolicy'.
	 */
	private JMenuItem removePolicyMenuItem = null;
	/**
	 * Object that represents the item 'ShowExpectedUtility'.
	 */
	private JMenuItem showExpectedUtilityMenuItem = null;
	/**
	 * Object that represents the item 'ShowOptimalPolicy'.
	 */
	private JMenuItem showOptimalPolicyMenuItem = null;
	/**
	 * Object that represents the item 'Expand'.
	 */
	private JMenuItem expandMenuItem = null;
	/**
	 * Object that represents the item 'Contract'.
	 */
	private JMenuItem contractMenuItem = null;
	/**
	 * Object that represents the item 'addFinding'.
	 */
	private JMenuItem addFindingMenuItem = null;
	/**
	 * Object that represents the item 'removeFinding'.
	 */
	private JMenuItem removeFindingMenuItem = null;
	// TODO OOPN start
	/**
	 * Object that represents the item 'Mark as Input'.
	 */
	private JMenuItem inputMenuItem = null;
	// TODO OOPN end
	private JMenuItem logMenuItem;

	/**
	 * Object that represents the item 'Temporal evolution'.
	 */
	private JMenuItem temporalEvolutionMenuItem;

	/**
	 * Object that represents the item 'Create node in next slice'.
	 */
	private JMenuItem nextSliceNodeMenuItem;

	/**
	 * This constructor creates a new instance.
	 *
	 * @param newListener  object that listens to the menu events.
	 * @param panel
	 * @param selectedNode
	 */
	public NodeContextualMenu(ActionListener newListener, VisualNode selectedNode, EditorPanel panel) {
		super(newListener);
		initialize();

        // Test if the node can be absorbed. Validate method returns true in that case
        Node node = selectedNode.getNode();
        setOptionEnabled(ActionCommands.ABSORB_NODE, AbsorbNodeValidator.validate(node));

		// Test if parents of the node can be absorbed. Validate method returns true in that case
		setOptionEnabled(ActionCommands.ABSORB_PARENTS, AbsorbParentsValidator.validate(node));

        if (selectedNode.getNode().getNodeType().equals(NodeType.DECISION)) {
			if (panel.getNetworkPanel().getWorkingMode() == NetworkPanel.EDITION_WORKING_MODE) {
				setDecisionNodeContextualMenuInEditionMode();
			} else {
				if (panel.getEvidenceCasesCompilationState(panel.getCurrentCase())) {
					setDecisionNodeContextualMenuInCompiledInferenceMode();
				} else {
					setDecisionNodeContextualMenuInNotCompiledInferenceMode();
				}
			}
		} else {
			setDefaultNodeContextualMenu();
		}

	}

	/**
	 * This method initialises this instance.
	 */
	private void initialize() {
		add(getCutMenuItem());
		add(getCopyMenuItem());
		add(getRemoveMenuItem());
        addSeparator();
        add(getAbsorbNodeMenuItem());
		addSeparator();
		add(getAbsorbParentsMenuItem());
		addSeparator();
		add(getPropertiesMenuItem());
		add(getEditPotentialMenuItem());
		addSeparator();
		add(getExpandMenuItem());
		add(getContractMenuItem());
		addSeparator();
		add(getAddFindingMenuItem());
		add(getRemoveFindingMenuItem());
		addSeparator();
		add(getTemporalEvolutionMenuItem());
		// addSeparator();
		// add(getLogMenuItem());
		// TODO OOPN start
		addSeparator();
		add(getInputMenuItem());
		// TODO OOPN start
	}

	/**
	 * This method sets the default contextual menu for nodes
	 */
	public void setDefaultNodeContextualMenu() {
		removeAll();
		add(getCutMenuItem());
		add(getCopyMenuItem());
		add(getRemoveMenuItem());
		addSeparator();
        add(getAbsorbNodeMenuItem());
        addSeparator();
		add(getAbsorbParentsMenuItem());
		addSeparator();
		add(getTemporalEvolutionMenuItem());
		add(getNextSliceNodeMenuItem());
		addSeparator();
		add(getPropertiesMenuItem());
		add(getEditPotentialMenuItem());
		addSeparator();
		add(getExpandMenuItem());
		add(getContractMenuItem());
		addSeparator();
		add(getAddFindingMenuItem());
		add(getRemoveFindingMenuItem());
		/*
		 * addSeparator(); add(getLogMenuItem());
		 */
		// TODO OOPN start
		//addSeparator ();
		//add (getInputMenuItem ());
		// TODO OOPN start
		pack();
	}

	/**
	 * This method sets the contextual menu for Decision nodes in Edition mode
	 */
	public void setDecisionNodeContextualMenuInEditionMode() {
		removeAll();
		add(getCutMenuItem());
		add(getCopyMenuItem());
		add(getRemoveMenuItem());
        addSeparator();
        add(getAbsorbNodeMenuItem());
		addSeparator();
		add(getAbsorbParentsMenuItem());
		addSeparator();
		add(getTemporalEvolutionMenuItem());
		add(getNextSliceNodeMenuItem());
		addSeparator();
		add(getPropertiesMenuItem());
		addSeparator();
		add(getExpandMenuItem());
		add(getContractMenuItem());
		addSeparator();
		add(getImposePolicyMenuItem());
		add(getEditPolicyMenuItem());
		add(getRemovePolicyMenuItem());
		//addSeparator();
		//add(getAddFindingMenuItem());
		//add(getRemoveFindingMenuItem());
		/*
		 * addSeparator(); add(getLogMenuItem());
		 */
		// TODO OOPN start
		//addSeparator ();
		//add (getInputMenuItem ());
		// TODO OOPN start
		pack();
	}

	/**
	 * This method sets the contextual menu for Decision nodes in Inference mode
	 * when the network is compiled
	 */
	public void setDecisionNodeContextualMenuInCompiledInferenceMode() {
		removeAll();
		add(getCutMenuItem());
		add(getCopyMenuItem());
		add(getRemoveMenuItem());
		addSeparator();
		add(getTemporalEvolutionMenuItem());
		addSeparator();
		add(getPropertiesMenuItem());
		addSeparator();
		add(getExpandMenuItem());
		add(getContractMenuItem());
		addSeparator();
		add(getShowExpectedUtilityMenuItem());
		add(getShowOptimalPolicyMenuItem());
		addSeparator();
		add(getAddFindingMenuItem());
		add(getRemoveFindingMenuItem());
		/*
		 * addSeparator(); add(getLogMenuItem());
		 */
		pack();
	}

	/**
	 * This method sets the ContextualMenu for Decision nodes in Inference mode
	 * when the network is not compiled
	 */
	public void setDecisionNodeContextualMenuInNotCompiledInferenceMode() {
		removeAll();
		add(getCutMenuItem());
		add(getCopyMenuItem());
		add(getRemoveMenuItem());
		addSeparator();
		add(getTemporalEvolutionMenuItem());
		addSeparator();
		add(getPropertiesMenuItem());
		addSeparator();
		add(getExpandMenuItem());
		add(getContractMenuItem());
		addSeparator();
		add(getAddFindingMenuItem());
		add(getRemoveFindingMenuItem());
		/*
		 * addSeparator(); add(getLogMenuItem());
		 */
		pack();
	}

	/*
	 * private JMenuItem getLogMenuItem() { if (logMenuItem == null) {
	 * logMenuItem = new LocalizedMenuItem (MenuItemNames.EDIT_LOG_MENUITEM,
	 * ActionCommands.LOG); logMenuItem.addActionListener(listener); } return
	 * logMenuItem; }
	 */

	/**
	 * This method initializes temporalEvolutionMenuItem.
	 *
	 * @return a new 'Temporal Evolution' menu item.
	 */
	private JMenuItem getTemporalEvolutionMenuItem() {
		if (temporalEvolutionMenuItem == null) {
			temporalEvolutionMenuItem = new LocalizedMenuItem(MenuItemNames.TEMPORAL_EVOLUTION_MENUITEM,
					ActionCommands.TEMPORAL_EVOLUTION_ACTION);
			temporalEvolutionMenuItem.addActionListener(listener);
		}
		return temporalEvolutionMenuItem;
	}

	/**
	 * This method initializes temporalEvolutionMenuItem.
	 *
	 * @return a new 'Temporal Evolution' menu item.
	 */
	private JMenuItem getNextSliceNodeMenuItem() {
		if (nextSliceNodeMenuItem == null) {
			nextSliceNodeMenuItem = new LocalizedMenuItem(MenuItemNames.NEXT_SLICE_NODE,
					ActionCommands.NEXT_SLICE_NODE);
			nextSliceNodeMenuItem.addActionListener(listener);
		}
		return nextSliceNodeMenuItem;
	}

	/**
	 * This method initializes cutMenuItem.
	 *
	 * @return a new 'Cut' menu item.
	 */
	private JMenuItem getCutMenuItem() {
		if (cutMenuItem == null) {
			cutMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_CUT_MENUITEM, ActionCommands.CLIPBOARD_CUT);
			cutMenuItem.addActionListener(listener);
		}
		return cutMenuItem;
	}

	/**
	 * This method initialises copyMenuItem.
	 *
	 * @return a new 'Copy' menu item.
	 */
	private JMenuItem getCopyMenuItem() {
		if (copyMenuItem == null) {
			copyMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_COPY_MENUITEM, ActionCommands.CLIPBOARD_COPY);
			copyMenuItem.addActionListener(listener);
		}
		return copyMenuItem;
	}

	/**
	 * This method initialises removeMenuItem.
	 *
	 * @return a new 'Remove' menu item.
	 */
	private JMenuItem getRemoveMenuItem() {
		if (removeMenuItem == null) {
			removeMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_REMOVE_MENUITEM, ActionCommands.OBJECT_REMOVAL);
			removeMenuItem.addActionListener(listener);
		}
		return removeMenuItem;
	}

    /**
     * This method initialises absorbNodeMenuItem.
     *
     * @return a new 'absorbNode' menu item.
     */
    private JMenuItem getAbsorbNodeMenuItem() {
        if (absorbNodeMenuItem == null) {
            absorbNodeMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_ABSORBNODE_MENUITEM, ActionCommands.ABSORB_NODE);
            absorbNodeMenuItem.addActionListener(listener);
        }
        return absorbNodeMenuItem;
    }

	/**
	 * This method initialises propertiesMenuItem.
	 *
	 * @return a new 'Properties' menu item.
	 */
	private JMenuItem getPropertiesMenuItem() {
		if (propertiesMenuItem == null) {
			propertiesMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_NODEPROPERTIES_MENUITEM,
					ActionCommands.NODE_PROPERTIES);
			propertiesMenuItem.addActionListener(listener);
		}
		return propertiesMenuItem;
	}

	/**
	 * This method initialises tableMenuItem.
	 *
	 * @return a new 'Table' menu item.
	 */
	private JMenuItem getEditPotentialMenuItem() {
		if (relationMenuItem == null) {
			relationMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_NODERELATION_MENUITEM,
					ActionCommands.EDIT_POTENTIAL);
			relationMenuItem.addActionListener(listener);
		}
		return relationMenuItem;
	}

	/**
	 * This method initialises imposePolicyMenuItem.
	 *
	 * @return a new 'ImposePolicy' menu item.
	 */
	private JMenuItem getImposePolicyMenuItem() {
		if (imposePolicyMenuItem == null) {
			imposePolicyMenuItem = new LocalizedMenuItem(MenuItemNames.DECISION_IMPOSE_POLICY_MENUITEM,
					ActionCommands.DECISION_IMPOSE_POLICY);
			imposePolicyMenuItem.addActionListener(listener);
		}
		return imposePolicyMenuItem;
	}

	/**
	 * This method initialises editPolicyMenuItem.
	 *
	 * @return a new 'EditPolicy' menu item.
	 */
	private JMenuItem getEditPolicyMenuItem() {
		if (editPolicyMenuItem == null) {
			editPolicyMenuItem = new LocalizedMenuItem(MenuItemNames.DECISION_EDIT_POLICY_MENUITEM,
					ActionCommands.DECISION_EDIT_POLICY);
			editPolicyMenuItem.addActionListener(listener);
		}
		return editPolicyMenuItem;
	}

	/**
	 * This method initialises removePolicyMenuItem.
	 *
	 * @return a new 'RemovePolicy' menu item.
	 */
	private JMenuItem getRemovePolicyMenuItem() {
		if (removePolicyMenuItem == null) {
			removePolicyMenuItem = new LocalizedMenuItem(MenuItemNames.DECISION_REMOVE_POLICY_MENUITEM,
					ActionCommands.DECISION_REMOVE_POLICY);
			removePolicyMenuItem.addActionListener(listener);
		}
		return removePolicyMenuItem;
	}

	/**
	 * This method initialises showExpectedUtilityMenuItem.
	 *
	 * @return a new 'ShowExpectedUtility' menu item.
	 */
	private JMenuItem getShowExpectedUtilityMenuItem() {
		if (showExpectedUtilityMenuItem == null) {
			showExpectedUtilityMenuItem = new LocalizedMenuItem(MenuItemNames.SHOW_EXPECTED_UTILITY_MENUITEM,
					ActionCommands.DECISION_SHOW_EXPECTED_UTILITY);
			showExpectedUtilityMenuItem.addActionListener(listener);
		}
		return showExpectedUtilityMenuItem;
	}

	/**
	 * This method initialises showOptimalPolicyMenuItem.
	 *
	 * @return a new 'ShowOptimalPolicy' menu item.
	 */
	private JMenuItem getShowOptimalPolicyMenuItem() {
		if (showOptimalPolicyMenuItem == null) {
			showOptimalPolicyMenuItem = new LocalizedMenuItem(MenuItemNames.SHOW_OPTIMAL_POLICY_MENUITEM,
					ActionCommands.DECISION_SHOW_OPTIMAL_POLICY);
			showOptimalPolicyMenuItem.addActionListener(listener);
		}
		return showOptimalPolicyMenuItem;
	}

	/**
	 * This method initialises expandMenuItem.
	 *
	 * @return a new 'Expand' menu item.
	 */
	private JMenuItem getExpandMenuItem() {
		if (expandMenuItem == null) {
			expandMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_EXPAND_NODE_MENUITEM,
					ActionCommands.NODE_EXPANSION);
			expandMenuItem.addActionListener(listener);
		}
		return expandMenuItem;
	}

	/**
	 * This method initialises contractMenuItem.
	 *
	 * @return a new 'Contract' menu item.
	 */
	private JMenuItem getContractMenuItem() {
		if (contractMenuItem == null) {
			contractMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_CONTRACT_NODE_MENUITEM,
					ActionCommands.NODE_CONTRACTION);
			contractMenuItem.addActionListener(listener);
		}
		return contractMenuItem;
	}

	/**
	 * This method initialises addFindingMenuItem.
	 *
	 * @return a new 'addFinding' menu item.
	 */
	private JMenuItem getAddFindingMenuItem() {
		if (addFindingMenuItem == null) {
			addFindingMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_ADD_FINDING_MENUITEM,
					ActionCommands.NODE_ADD_FINDING);
			addFindingMenuItem.addActionListener(listener);
		}
		return addFindingMenuItem;
	}

	/**
	 * This method initialises removeFindingMenuItem.
	 *
	 * @return a new 'removeFinding' menu item.
	 */
	private JMenuItem getRemoveFindingMenuItem() {
		if (removeFindingMenuItem == null) {
			removeFindingMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_REMOVE_FINDING_MENUITEM,
					ActionCommands.NODE_REMOVE_FINDING);
			removeFindingMenuItem.addActionListener(listener);
		}
		return removeFindingMenuItem;
	}

	// TODO OOPN start

	/**
	 * This method initialises inputMenuItem.
	 *
	 * @return a new 'Input' menu item.
	 */
	private JMenuItem getInputMenuItem() {
		if (inputMenuItem == null) {
			inputMenuItem = new JCheckBoxMenuItem(MenuLocalizer.getLabel(MenuItemNames.EDIT_MARKASINPUT_MENUITEM));
			inputMenuItem.setActionCommand(ActionCommands.MARK_AS_INPUT);
			inputMenuItem.addActionListener(listener);
		}
		return inputMenuItem;
	}
	
    /**
     * This method initialises AbsorbParentsMenuItem.
     *
     * @return a new 'AbsorbParents' menu item.
     */
    private JMenuItem getAbsorbParentsMenuItem() {
        if (absorbParentsMenuItem == null) {
        	absorbParentsMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_ABSORBPARENTS_MENUITEM, ActionCommands.ABSORB_PARENTS);
        	absorbParentsMenuItem.addActionListener(listener);
        }
        
        return absorbParentsMenuItem;
    }

	// TODO OOPN end

	/**
	 * Returns the component that corresponds to an action command.
	 *
	 * @param actionCommand action command that identifies the component.
	 * @return a components identified by the action command.
	 */
    @Override protected JComponent getJComponentActionCommand(String actionCommand) {
        JComponent component = null;
        switch (actionCommand) {
            case ActionCommands.CLIPBOARD_CUT:
                component = cutMenuItem;
                break;
            case ActionCommands.CLIPBOARD_COPY:
                component = copyMenuItem;
                break;
            case ActionCommands.OBJECT_REMOVAL:
                component = removeMenuItem;
                break;
            case ActionCommands.ABSORB_NODE:
                component = absorbNodeMenuItem;
                break;
            case ActionCommands.ABSORB_PARENTS:
                component = absorbParentsMenuItem;
                break;
            case ActionCommands.NODE_PROPERTIES:
                component = propertiesMenuItem;
                break;
            case ActionCommands.EDIT_POTENTIAL:
                component = relationMenuItem;
                break;
            case ActionCommands.DECISION_IMPOSE_POLICY:
                component = imposePolicyMenuItem;
                break;
            case ActionCommands.DECISION_EDIT_POLICY:
                component = editPolicyMenuItem;
                break;
            case ActionCommands.DECISION_REMOVE_POLICY:
                component = removePolicyMenuItem;
                break;
            case ActionCommands.DECISION_SHOW_EXPECTED_UTILITY:
                component = showExpectedUtilityMenuItem;
                break;
            case ActionCommands.DECISION_SHOW_OPTIMAL_POLICY:
                component = showOptimalPolicyMenuItem;
                break;
            case ActionCommands.NODE_EXPANSION:
                component = expandMenuItem;
                break;
            case ActionCommands.NODE_CONTRACTION:
                component = contractMenuItem;
                break;
            case ActionCommands.NODE_ADD_FINDING:
                component = addFindingMenuItem;
                break;
            case ActionCommands.NODE_REMOVE_FINDING:
                component = removeFindingMenuItem;
                break;
            case ActionCommands.LOG:
                component = logMenuItem;
                // TODO OOPN start
                break;
            case ActionCommands.MARK_AS_INPUT:
                component = inputMenuItem;
                // TODO OOPN end
                break;
            case ActionCommands.TEMPORAL_EVOLUTION_ACTION:
                component = temporalEvolutionMenuItem;
                break;
            case ActionCommands.NEXT_SLICE_NODE:
                component = nextSliceNodeMenuItem;
                break;
        }
		return component;
	}
}
