/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.openmarkov.gui.localize.LocaleChangeEvent;
import org.openmarkov.gui.localize.LocaleChangeListener;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasicImpl;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * This class is used to set the common features of all contextual menus of the
 * application.
 *
 * @author jmendoza
 * @author jlgozalo
 * @version 1.1 adding StringResourceLocaleChangeListener
 */
public abstract class ContextualMenu extends JPopupMenu implements MenuToolBarBasic, LocaleChangeListener {

	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = -792738738895619891L;
	/**
	 * Object that listen to the user's actions.
	 */
	protected ActionListener listener;

	/**
	 * This method initialises this instance.
	 *
	 * @param newListener listener that listen to the user's actions.
	 */
	public ContextualMenu(ActionListener newListener) {

		super();
		listener = newListener;
		StringDatabase.getUniqueInstance().addLocaleChangeListener(this);

	}

	/**
	 * Returns the component that correspond to an action command.
	 *
	 * @param actionCommand action command that identifies the component.
	 * @return a components identified by the action command.
	 */
	protected abstract JComponent getJComponentActionCommand(String actionCommand);

	/**
	 * Enables or disabled an option identified by an action command.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to enable the option, false to disable.
	 */
	public void setOptionEnabled(String actionCommand, boolean b) {

		MenuToolBarBasicImpl.setOptionEnabled(getJComponentActionCommand(actionCommand), b);
	}

	/**
	 * Selects or unselects an option identified by an action command. Only
	 * selects or unselects the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param b             true to select the option, false to unselect.
	 */
	public void setOptionSelected(String actionCommand, boolean b) {

		MenuToolBarBasicImpl.setOptionSelected(getJComponentActionCommand(actionCommand), b);

		if (getJComponentActionCommand(actionCommand) instanceof JCheckBoxMenuItem) {
			((JCheckBoxMenuItem) getJComponentActionCommand(actionCommand)).setState(b);
		}
	}

	/**
	 * Adds a text to the label of an option identified by an action command.
	 * Only adds a text to the components that are AbstractButton.
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param text          text to add to the label of the options. If null, nothing is
	 *                      added.
	 */
	public void addOptionText(String actionCommand, String text) {

	}

	/**
	 * process a change in the String Resource Locale, settings all the labels
	 * menus, and strings in the component to the new selected language
	 */
	public void processLocaleChange(LocaleChangeEvent event) {

		StringDatabase.getUniqueInstance().allComponentsUpdateSetText(this);
		repaint();
	}

	/**
	 * Changes the text of menu item
	 *
	 * @param actionCommand action command that identifies the option.
	 * @param text          text to add to the label.
	 */
	public void setText(String actionCommand, String text) {

		JComponent component = getJComponentActionCommand(actionCommand);
		MenuToolBarBasicImpl.setText(component, text);

	}

}
