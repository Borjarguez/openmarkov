/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.openmarkov.core.action.NodeStateEdit;
import org.openmarkov.core.action.StateAction;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.gui.action.NodePartitionedIntervalEdit;
import org.openmarkov.gui.action.PartitionedIntervalEdit;
import org.openmarkov.gui.dialog.common.KeyTablePanel;
import org.openmarkov.gui.loader.element.IconLoader;
import org.openmarkov.gui.localize.StringDatabase;
import org.openmarkov.gui.util.GUIDefaultStates;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * This class implements a panel to encapsulate a Discretize Table with the
 * following features:
 * <ul>
 * <li>Its elements, except the first column that will be hidden, are
 * modifiable.</li>
 * <li>New elements can be added, creating a new key row with empty data.</li>
 * <li>The key data (first column) consist of a key string following of the
 * index of the row. This row is hidden</li>
 * <li>The information of a row (except the first column) can be taken up or
 * down.</li>
 * <li>The rows can be removed.</li>
 * <li>The initial lower value of a row is the upper value of the previous row
 * plus a delta, if the model monotony is DOWN</li>
 * <li>The initial upper value of a row is the lower value of the previous row
 * plus a delta, if the model monotony is UP</li>
 * <li>Infinite positive and negative are allowed as values through specific
 * buttons</li>
 * </ul>
 *
 * @author jlgozalo
 * @author myebra
 * @version 1.0 29 Jun 2009
 */
public class DiscretizeTablePanel extends KeyTablePanel implements TableModelListener, MouseListener {

	/**
	 * number of the columns in this type of table
	 */
	protected static final int ID_COLUMN_INDEX = 0;
	protected static final int INTERVAL_NAME_COLUMN_INDEX = 1;
	protected static final int LOWER_BOUND_SYMBOL_COLUMN_INDEX = 2;
	protected static final int LOWER_BOUND_VALUE_COLUMN_INDEX = 3;
	protected static final int VALUES_SEPARATOR_COLUMN_INDEX = 4;
	protected static final int UPPER_BOUND_VALUE_COLUMN_INDEX = 5;
	protected static final int UPPER_BOUND_SYMBOL_COLUMN_INDEX = 6;
	/**
	 * default serial id
	 */
	private static final long serialVersionUID = 1L;
	private static final String INFINITY = "\u221E";
	private static final String NEGATIVE_INFINITY = "-" + "\u221E";
	/**
	 * ComboBox for intervals symbols
	 */
	private static final String[] intervalLowerSymbols = new String[] { "[", "(" };
	private static final String[] intervalUpperSymbols = new String[] { "]", ")" };
	/**
	 * Size of the states table
	 */
	private final int STATES_TABLE_WIDTH = 406;
	/**
	 * Width of the parenthesis and brackets in the state's table
	 */
	private final int LIMITS_WIDTH = 40;
	/**
	 * Width of the separator used in intervals
	 */
	private final int SEPARATOR_WIDTH = 10;
	private final int DISCRETIZED_STATES_WIDTH = 120;
	/**
	 * Button to select variable states.
	 */
	protected JButton standardDomainButton = null;
	/**
	 * String database
	 */
	protected StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
	protected Node node;
	private JComboBox<String> lowerSymbolComboBox = null;
	private JComboBox<String> upperSymbolComboBox = null;
	/**
	 * monotony of the items in the table - true = up; false=down;
	 */
	private boolean upMonotony = true;   // default=UP
	/**
	 * Key prefix (required to maintain the index of the table even if it is not
	 * shown to the user)
	 */
	private String keyPrefix = null;
	/**
	 * Infinite Positive Button
	 */
	private JButton positiveInfinityButton = null;
	/**
	 * Infinite Positive Button
	 */
	private JButton negativeInfinityButton = null;
	/**
	 * discretize table model
	 */
	private DiscretizeTableModel discretizeTableModel = null;

	/**
	 * default constructor
	 *
	 *
	 */
	public DiscretizeTablePanel(String[] newColumns, Node node) {
		this(newColumns, new Object[0][0], "s", node);
		// s = keyPrefix for id column; not shown to user
	}

	/**
	 * constructor with parameters
	 */
	public DiscretizeTablePanel(String[] newColumns, Object[][] noKeyData, String newKeyPrefix, Node node) {
		super(newColumns, new Object[0][0], true, true);// , notifier);
		this.node = node;
		keyPrefix = newKeyPrefix;
		initialize();
		setData(noKeyData); // also it is setting the model for the table
		// define the look and feel for the table element
		defineTableLookAndFeel();
		// define specific listeners
		defineTableSpecificListeners();
		getTableModel().addTableModelListener(this);
	}

	/**
	 * This method initializes this instance.
	 */
	@Override protected void initialize() {
		// define the border and layout for the panel
		setBorder(new EmptyBorder(0, 0, 0, 0));
		final GroupLayout groupLayout = new GroupLayout((JComponent) this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addContainerGap()
						.addComponent(getValuesTableScrollPane(), GroupLayout.PREFERRED_SIZE, STATES_TABLE_WIDTH,
								GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(getButtonPanel(), GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(
				groupLayout.createSequentialGroup().addContainerGap().addGroup(
						groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(getValuesTableScrollPane(), GroupLayout.Alignment.LEADING,
										GroupLayout.DEFAULT_SIZE,
										/* 131 */200, Short.MAX_VALUE)
								.addComponent(getButtonPanel(), GroupLayout.PREFERRED_SIZE,
										/* 131 */200, Short.MAX_VALUE)).addGap(24, 24, 24)));
		setLayout(groupLayout);
	}

	/**
	 * This method initializes tableModel.
	 *
	 * @return a new tableModel.
	 */
	@Override protected DiscretizeTableModel getTableModel() {
		if (discretizeTableModel == null) {
			discretizeTableModel = new DiscretizeTableModel(data, columns);
		}
		return discretizeTableModel;
	}

	/**
	 * defines the look and feel of the table (column width, etc...)
	 */
	protected void defineTableLookAndFeel() {
		// center the data in all columns
		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
		tcr.setHorizontalAlignment(SwingConstants.CENTER);
		DefaultTableCellRenderer statesRender = new DefaultTableCellRenderer();
		statesRender.setHorizontalAlignment(SwingConstants.LEFT);
		int maxColumn = valuesTable.getColumnModel().getColumnCount();

		for (int i = 1; i < maxColumn; i++) {
			TableColumn aColumn = valuesTable.getColumnModel().getColumn(i);
			aColumn.setCellRenderer(tcr);
			valuesTable.getTableHeader().getColumnModel().getColumn(i).setCellRenderer(tcr);
		}

		// set special columns for each variable type
		if (node.getVariable().getVariableType() == VariableType.NUMERIC) {

			//In Numeric cases we must hide the two first columns
			valuesTable.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(0);
			valuesTable.getTableHeader().getColumnModel().getColumn(0).setMinWidth(0);
			valuesTable.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(0);

			valuesTable.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(0);
			valuesTable.getTableHeader().getColumnModel().getColumn(1).setMinWidth(0);
			valuesTable.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(0);

		} else if (node.getVariable().getVariableType() == VariableType.FINITE_STATES) {
			//In Finite States we set all the width of the table
			TableColumn aColumn = valuesTable.getColumnModel().getColumn(1);
			aColumn.setCellRenderer(statesRender);
			valuesTable.getColumnModel().getColumn(1).setPreferredWidth(STATES_TABLE_WIDTH);
			valuesTable.getColumnModel().getColumn(1).setMaxWidth(STATES_TABLE_WIDTH);
			valuesTable.getColumnModel().getColumn(1).setMinWidth(STATES_TABLE_WIDTH);
			// The rest of the columns will be hided
			for (int i = 2; i < maxColumn; i++) {
				TableColumn columni = valuesTable.getColumnModel().getColumn(i);
				columni.setCellRenderer(tcr);
				columni.setPreferredWidth(0);
				columni.setMaxWidth(0);
				columni.setMinWidth(0);
				valuesTable.getTableHeader().getColumnModel().getColumn(i).setCellRenderer(tcr);
			}
		} else if (node.getVariable().getVariableType() == VariableType.DISCRETIZED) {
			TableColumn aColumn = valuesTable.getColumnModel().getColumn(1);
			aColumn.setCellRenderer(statesRender);

			//If the variable type is discrete, we hide the first column and fix the width of the state name column
			valuesTable.getColumnModel().getColumn(0).setPreferredWidth(0);
			valuesTable.getColumnModel().getColumn(0).setMaxWidth(0);
			valuesTable.getColumnModel().getColumn(0).setMinWidth(0);

			valuesTable.getColumnModel().getColumn(1).setPreferredWidth(DISCRETIZED_STATES_WIDTH);
			valuesTable.getColumnModel().getColumn(1).setMaxWidth(DISCRETIZED_STATES_WIDTH);
			valuesTable.getColumnModel().getColumn(1).setMinWidth(DISCRETIZED_STATES_WIDTH);
		}
		// set Columns = Up and Low limits
		if (node.getVariable().getVariableType() == VariableType.NUMERIC
				|| node.getVariable().getVariableType() == VariableType.DISCRETIZED) {
			lowerSymbolComboBox = getLowerSymbolComboBox();
			upperSymbolComboBox = getUpperSymbolComboBox();
			TableColumn lowLimitSymbolColumn = valuesTable.getColumnModel().getColumn(LOWER_BOUND_SYMBOL_COLUMN_INDEX);
			lowLimitSymbolColumn.setCellEditor(new DefaultCellEditor(lowerSymbolComboBox));
			lowLimitSymbolColumn.setCellRenderer(new DiscretizeComboBoxRenderer(intervalLowerSymbols));
			lowLimitSymbolColumn.setMinWidth(LIMITS_WIDTH);
			lowLimitSymbolColumn.setPreferredWidth(LIMITS_WIDTH);
			lowLimitSymbolColumn.setMaxWidth(LIMITS_WIDTH);
			TableColumn upperLimitSymbolColumn = valuesTable.getColumnModel()
					.getColumn(UPPER_BOUND_SYMBOL_COLUMN_INDEX);
			upperLimitSymbolColumn.setCellEditor(new DefaultCellEditor(upperSymbolComboBox));
			upperLimitSymbolColumn.setCellRenderer(new DiscretizeComboBoxRenderer(intervalUpperSymbols));
			upperLimitSymbolColumn.setMinWidth(LIMITS_WIDTH);
			upperLimitSymbolColumn.setPreferredWidth(40);
			upperLimitSymbolColumn.setMaxWidth(LIMITS_WIDTH);
			// set Column = valuesSeparator = ","
			TableColumn valuesSeparatorColumn = valuesTable.getColumnModel().getColumn(VALUES_SEPARATOR_COLUMN_INDEX);
			valuesSeparatorColumn.setMinWidth(SEPARATOR_WIDTH);
			valuesSeparatorColumn.setPreferredWidth(SEPARATOR_WIDTH);
			valuesSeparatorColumn.setMaxWidth(SEPARATOR_WIDTH);
		}
	}

	private JComboBox<String> getLowerSymbolComboBox() {
		if (lowerSymbolComboBox == null) {
			lowerSymbolComboBox = new JComboBox<>(intervalLowerSymbols);
		}
		return lowerSymbolComboBox;
	}

	private JComboBox<String> getUpperSymbolComboBox() {
		if (upperSymbolComboBox == null) {
			upperSymbolComboBox = new JComboBox<>(intervalUpperSymbols);
		}
		return upperSymbolComboBox;
	}

	/**
	 * Method to define the specific listeners in this table (not defined in the
	 * common KeyTable hierarchy
	 */
	protected void defineTableSpecificListeners() {
		valuesTable.addMouseListener(this);
	}

	/**
	 * This method is used to change the interval's type in a discretize Table
	 * To closed from opened To opened from closed
	 */
	private void changeLimitIntervalDiscretize(int row, int column) {
		if (column == LOWER_BOUND_SYMBOL_COLUMN_INDEX || column == UPPER_BOUND_SYMBOL_COLUMN_INDEX) {
			boolean lower = false;
			String value = (String) valuesTable.getValueAt(row, column);
			if (column == LOWER_BOUND_SYMBOL_COLUMN_INDEX) {
				lower = true;
				if (value.equals("(")) {
					if (valuesTable.getValueAt(row, LOWER_BOUND_VALUE_COLUMN_INDEX) == INFINITY
							|| valuesTable.getValueAt(row, LOWER_BOUND_VALUE_COLUMN_INDEX) == NEGATIVE_INFINITY) {
						JOptionPane.showMessageDialog(this, "Infinity can not belong to the interval");
					} else if (row + 1 < valuesTable.getRowCount()
							&& valuesTable.getValueAt(row + 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX) == "["
							&& valuesTable.getValueAt(row + 1, LOWER_BOUND_VALUE_COLUMN_INDEX) == valuesTable
							.getValueAt(row + 1, UPPER_BOUND_VALUE_COLUMN_INDEX)) {
						JOptionPane.showMessageDialog(this, "Not permitted action. You must change limit values first");
					} else {
						valuesTable.setValueAt("[", row, column);
						if (row + 1 < valuesTable.getRowCount()) {
							valuesTable.setValueAt(")", row + 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX);
						}
						NodePartitionedIntervalEdit nodePartitionedIntervalEdit = new NodePartitionedIntervalEdit(node,
								StateAction.MODIFY_DELIMITER_INTERVAL, row, lower);
						try {
							node.getProbNet().doEdit(nodePartitionedIntervalEdit);
							// @ 2014/11/18. Issue 145.
							// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
							// Propagation of the domain in related variables in temporal models
							propagateNodePartitionedIntervalEditRelatedVariables(StateAction.MODIFY_DELIMITER_INTERVAL,
									row, lower);
							//
						} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
							JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
									stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
						}
					}
				} else if (value.equals("[")) {
					if (valuesTable.getValueAt(row, LOWER_BOUND_VALUE_COLUMN_INDEX) == valuesTable
							.getValueAt(row, UPPER_BOUND_VALUE_COLUMN_INDEX)
							&& valuesTable.getValueAt(row, UPPER_BOUND_SYMBOL_COLUMN_INDEX) == "]") {
						JOptionPane.showMessageDialog(this, "Not permitted action. You must change limit values first");
					} else {
						valuesTable.setValueAt("(", row, column);
						if (row + 1 < valuesTable.getRowCount()) {
							valuesTable.setValueAt("]", row + 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX);
						}
						NodePartitionedIntervalEdit nodePartitionedIntervalEdit = new NodePartitionedIntervalEdit(node,
								StateAction.MODIFY_DELIMITER_INTERVAL, row, lower);
						try {
							node.getProbNet().doEdit(nodePartitionedIntervalEdit);
							// @ 2014/11/18. Issue 145.
							// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
							// Propagation of the domain in related variables in temporal models
							propagateNodePartitionedIntervalEditRelatedVariables(StateAction.MODIFY_DELIMITER_INTERVAL,
									row, lower);
							//
						} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
							JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
									stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			if (column == UPPER_BOUND_SYMBOL_COLUMN_INDEX) {
				if (value.equals(")")) {
					if (valuesTable.getValueAt(row, UPPER_BOUND_VALUE_COLUMN_INDEX) == INFINITY
							|| valuesTable.getValueAt(row, UPPER_BOUND_VALUE_COLUMN_INDEX) == NEGATIVE_INFINITY) {
						JOptionPane.showMessageDialog(this, "Infinity can not belong to the interval");
					} else if (row > 0 && valuesTable.getValueAt(row - 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX) == "]"
							&& valuesTable.getValueAt(row - 1, LOWER_BOUND_VALUE_COLUMN_INDEX) == valuesTable
							.getValueAt(row - 1, UPPER_BOUND_VALUE_COLUMN_INDEX)) {
						JOptionPane.showMessageDialog(this, "Not permitted action. You must change limit values first");
					} else {
						valuesTable.setValueAt("]", row, column);
						if (row > 0) {
							valuesTable.setValueAt("(", row - 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX);
						}
						NodePartitionedIntervalEdit nodePartitionedIntervalEdit = new NodePartitionedIntervalEdit(node,
								StateAction.MODIFY_DELIMITER_INTERVAL, row, lower);
						try {
							node.getProbNet().doEdit(nodePartitionedIntervalEdit);
							// @ 2014/11/18. Issue 145.
							// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
							// Propagation of the domain in related variables in temporal models
							propagateNodePartitionedIntervalEditRelatedVariables(StateAction.MODIFY_DELIMITER_INTERVAL,
									row, lower);
							//
						} catch (ConstraintViolationException | DoEditException | NonProjectablePotentialException | WrongCriterionException e) {
							JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
									stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
						}
					}
				} else if (value.equals("]")) {
					if (valuesTable.getValueAt(row, LOWER_BOUND_VALUE_COLUMN_INDEX) == valuesTable
							.getValueAt(row, UPPER_BOUND_VALUE_COLUMN_INDEX)
							&& valuesTable.getValueAt(row, LOWER_BOUND_SYMBOL_COLUMN_INDEX) == "[") {
						JOptionPane.showMessageDialog(this, "Not permitted action. You must change limit values first");
					} else {
						valuesTable.setValueAt(")", row, column);
						if (row > 0) {
							valuesTable.setValueAt("[", row - 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX);
						}
						NodePartitionedIntervalEdit nodePartitionedIntervalEdit = new NodePartitionedIntervalEdit(node,
								StateAction.MODIFY_DELIMITER_INTERVAL, row, lower);
						try {
							node.getProbNet().doEdit(nodePartitionedIntervalEdit);
							// @ 2014/11/18. Issue 145.
							// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
							// Propagation of the domain in related variables in temporal models
							propagateNodePartitionedIntervalEditRelatedVariables(StateAction.MODIFY_DELIMITER_INTERVAL,
									row, lower);
							//
						} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
							JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
									stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		}
	}

	/**
	 * method to control and change the values and symbols when user changes the
	 * values and limits in the table, depending upon the type of monotony
	 */
	protected void checkIntervalDiscretize(String currentState, int row, int column, boolean upMonotony) {
		// double aux, aux2;
		if (upMonotony) { // monotony UP
			if (row != valuesTable.getRowCount() - 1 && column == UPPER_BOUND_SYMBOL_COLUMN_INDEX) {
				if (currentState.equals(")")) {
					valuesTable.setValueAt("[", row + 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX);
				} else {
					valuesTable.setValueAt("(", row + 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX);
				}
			}
			if (row != 0 && column == LOWER_BOUND_SYMBOL_COLUMN_INDEX) {
				if (currentState.equals("(")) {
					valuesTable.setValueAt("]", row - 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX);
				} else {
					valuesTable.setValueAt(")", row - 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX);
				}
			}
		} else { // Down monotony
			if (row != 0 && column == UPPER_BOUND_SYMBOL_COLUMN_INDEX) {
				if (currentState.equals(")")) {
					valuesTable.setValueAt("[", row - 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX);
				} else {
					valuesTable.setValueAt("(", row - 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX);
				}
			}
			if (row != valuesTable.getRowCount() - 1 && column == LOWER_BOUND_SYMBOL_COLUMN_INDEX) {
				if (currentState.equals("(")) {
					valuesTable.setValueAt("]", row + 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX);
				} else {
					valuesTable.setValueAt(")", row + 1, UPPER_BOUND_SYMBOL_COLUMN_INDEX);
				}
			}
		}
	}

	/**
	 * This method initializes buttonPanel.
	 *
	 * @return a new button panel.
	 */
	@Override protected JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setName("DiscretizeTablePanel.buttonPanel");
			final GroupLayout groupLayout = new GroupLayout((JComponent) buttonPanel);
			groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(
					groupLayout.createSequentialGroup().addGroup(
							groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(getStandardDomainButton(), GroupLayout.DEFAULT_SIZE, 55,
											Short.MAX_VALUE)
									.addComponent(getAddValueButton(), GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getDownValueButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getUpValueButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getRemoveValueButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getPositiveInfinityButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
									.addComponent(getNegativeInfinityButton(), GroupLayout.Alignment.LEADING,
											GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)).addContainerGap()));
			groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
					groupLayout.createSequentialGroup().addComponent(getStandardDomainButton()).addGap(5, 5, 5)
							.addComponent(getAddValueButton()).addGap(5, 5, 5).addComponent(getRemoveValueButton())
							.addGap(5, 5, 5).addComponent(getUpValueButton()).addGap(5, 5, 5)
							.addComponent(getDownValueButton()).addGap(5, 5, 5)
							.addComponent(getPositiveInfinityButton()).addGap(5, 5, 5)
							.addComponent(getNegativeInfinityButton()).addGap(48, 48, 48)));
			buttonPanel.setLayout(groupLayout);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes upValueButton.
	 *
	 * @return a new up value button.
	 */
	public JButton getStandardDomainButton() {
		if (standardDomainButton == null) {
			standardDomainButton = new JButton();
			standardDomainButton.setName("KeyTablePanel.standardDomainButton");
			standardDomainButton.setText(stringDatabase.getString("StandardDomain.Text.Label"));
			standardDomainButton.setVisible(true);
			standardDomainButton.setEnabled(true);
			standardDomainButton.setActionCommand("StandardDomain");
			// standardDomainButton.addActionListener( this );
		}
		return standardDomainButton;
	}

	/**
	 * This method initializes infinitePositiveDoubleButton.
	 *
	 * @return a new positive infinite value button.
	 */
	public JButton getPositiveInfinityButton() {
		if (positiveInfinityButton == null) {
			positiveInfinityButton = new JButton();
			positiveInfinityButton.setName("DiscretizeTablePanel.jButtonInfinitePositiveDouble");
			positiveInfinityButton.setText(stringDatabase.getString("InfinitePositive.Text.Label"));
			positiveInfinityButton.setIcon(iconLoader.load(IconLoader.ICON_INFINITE_POSITIVE_ENABLED));
			// jButtonInfinitePositiveDouble.setVisible(reorderEnabled);
			positiveInfinityButton.setEnabled(false);
			positiveInfinityButton.addActionListener(this);
			positiveInfinityButton.setVisible(false);
		}
		return positiveInfinityButton;
	}

	/**
	 * This method initializes infiniteNegativeDoubleButton.
	 *
	 * @return a new negative infinite value button.
	 */
	public JButton getNegativeInfinityButton() {
		if (negativeInfinityButton == null) {
			negativeInfinityButton = new JButton();
			negativeInfinityButton.setName("DiscretizeTablePanel.jButtonInfiniteNegativeDouble");
			negativeInfinityButton.setText(stringDatabase.getString("InfiniteNegative.Text.Label"));
			negativeInfinityButton.setIcon(iconLoader.load(IconLoader.ICON_INFINITE_NEGATIVE_ENABLED));
			// jButtonInfiniteNegativeDouble.setVisible(reorderEnabled);
			negativeInfinityButton.setEnabled(false);
			negativeInfinityButton.addActionListener(this);
			negativeInfinityButton.setVisible(false);
		}
		return negativeInfinityButton;
	}

	/**
	 *
	 */
	public int getLowerLimitSymbolColumnNum() {
		return LOWER_BOUND_SYMBOL_COLUMN_INDEX;
	}

	/**
	 * @return the upMonotony
	 */
	public boolean isUpMonotony() {
		return upMonotony;
	}

	/**
	 * @param upMonotony the upMonotony to set
	 */
	public void setUpMonotony(boolean upMonotony) {
		this.upMonotony = upMonotony;
	}

	/**
	 * execute the change in the table when a set of default states have been
	 * selected in the combo box
	 */
	public void setNewDataInTable(int selectedIndex) {
		Object[][] newData = null;
		switch (selectedIndex) {
		case 0: // present-absent
			if (upMonotony) {
				Object[][] auxData = { { GUIDefaultStates.getString("absent"), "[", 0.0, ",", 2.0, "]" },
						{ GUIDefaultStates.getString("present"), "(", 2.0, ",", 4.0, "]" } };
				newData = auxData;
			} else {
				Object[][] auxData = { { GUIDefaultStates.getString("present"), "[", 2.0, ",", 4.0, ")" },
						{ GUIDefaultStates.getString("absent"), "[", 0.0, ",", 2.0, ")" } };
				newData = auxData;
			}
			break;
		case 1: // yes-no
			if (upMonotony) {
				Object[][] auxData = { { GUIDefaultStates.getString("yes"), "[", 0.0, ",", 2.0, "]" },
						{ GUIDefaultStates.getString("no"), "(", 2.0, ",", 4.0, "]" } };
				newData = auxData;
			} else {
				Object[][] auxData = { { GUIDefaultStates.getString("yes"), "[", 2.0, ",", 4.0, ")" },
						{ GUIDefaultStates.getString("no"), "[", 0.0, ",", 2.0, ")" } };
				newData = auxData;
			}
			break;
		case 2: // positive-negative
			if (upMonotony) {
				Object[][] auxData = { { GUIDefaultStates.getString("positive"), "[", 0.0, ",", 2.0, "]" },
						{ GUIDefaultStates.getString("negative"), "(", 2.0, ",", 4.0, "]" } };
				newData = auxData;
			} else {
				Object[][] auxData = { { GUIDefaultStates.getString("positive"), "[", 2.0, ",", 4.0, ")" },
						{ GUIDefaultStates.getString("negative"), "[", 0.0, ",", 2.0, ")" } };
				newData = auxData;
			}
			break;
		case 3: // severe-moderate-mild-absent
			if (upMonotony) {
				Object[][] auxData = { { GUIDefaultStates.getString("severe"), "[", 0.0, ",", 2.0, "]" },
						{ GUIDefaultStates.getString("moderate"), "(", 2.0, ",", 4.0, "]" },
						{ GUIDefaultStates.getString("mild"), "(", 4.0, ",", 6.0, "]" },
						{ GUIDefaultStates.getString("absent"), "(", 6.0, ",", 8.0, "]" } };
				newData = auxData;
			} else {
				Object[][] auxData = { { GUIDefaultStates.getString("severe"), "[", 6.0, ",", 8.0, ")" },
						{ GUIDefaultStates.getString("moderate"), "[", 4.0, ",", 6.0, ")" },
						{ GUIDefaultStates.getString("mild"), "[", 2.0, ",", 4.0, ")" },
						{ GUIDefaultStates.getString("absent"), "[", 0.0, ",", 2.0, ")" } };
				newData = auxData;
			}
			break;
		case 4: // high-medium-low
			if (upMonotony) {
				Object[][] auxData = { { GUIDefaultStates.getString("high"), "[", 0.0, ",", 2.0, "]" },
						{ GUIDefaultStates.getString("medium"), "(", 2.0, ",", 4.0, "]" },
						{ GUIDefaultStates.getString("low"), "(", 4.0, ",", 6.0, "]" } };
				newData = auxData;
			} else {
				Object[][] auxData = { { GUIDefaultStates.getString("high"), "[", 4.0, ",", 6.0, ")" },
						{ GUIDefaultStates.getString("medium"), "[", 2.0, ",", 4.0, ")" },
						{ GUIDefaultStates.getString("low"), "[", 0.0, ",", 2.0, ")" } };
				newData = auxData;
			}
			break;
		default: // nonamed
			Object[][] auxData = { { GUIDefaultStates.getString("nonamed"), "(", Double.NEGATIVE_INFINITY, ",",
					Double.POSITIVE_INFINITY, ")" } };
			newData = auxData;
			break;
		}
		setData(newData);
	}

	/**
	 * execute the change in the table when a set of default states have been
	 * selected in the combo box
	 */
	public void setPartitionedInterval() {
		PartitionedInterval partitionInterval = node.getVariable().getPartitionedInterval();
		Object[][] intervalTable = partitionInterval.convertToTableFormat();
		State states[] = node.getVariable().getStates();
		int rows = intervalTable.length;
		// invert states to display in the correct order
		State reorderedStates[] = states.clone();
		Collections.reverse(Arrays.asList(reorderedStates));
		for (int i = 0; i < rows; i++) {
			intervalTable[i][0] = GUIDefaultStates.getString(reorderedStates[i].getName());
		}
		setData(intervalTable);
	}

	public void setDataFromPartitionedInterval(PartitionedInterval partitionInterval, State[] states) {
		Object[][] data;
		int i = 0;
		int numIntervals = 0;
		int numColumns = 6; // name-symbol-value-separator-value-symbol
		String[] limits;
		boolean[] belongsToLeftSide;
		numIntervals = partitionInterval.getNumSubintervals();
		double values[] = partitionInterval.getLimits();
		limits = convertToStringLimitValues(values, Double.toString(node.getVariable().getPrecision()));
		belongsToLeftSide = partitionInterval.getBelongsToLeftSide();
		data = new Object[numIntervals][numColumns];
		for (i = 0; i < numIntervals; i++) {
			int row = numIntervals - i - 1;
			data[row][0] = GUIDefaultStates.getString(states[i].getName()); // name
			// 2015/06/17 - Issue #256
			// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/256/numeric-variable-with-closed-interval
			// low interval symbol
			if (limits[i].equals(NEGATIVE_INFINITY)) {
				data[row][1] = "(";
			} else {
				data[row][1] = (belongsToLeftSide[i] ? "(" : "[");
				// symbol
			}
			data[row][2] = limits[i]; // low interval value
			data[row][3] = ","; // separator ","
			data[row][4] = limits[i + 1]; // high interval value
			// high interval symbol
			if (limits[i + 1].equals(INFINITY)) {
				data[row][5] = ")";
			} else {
				data[row][5] = (belongsToLeftSide[i + 1] ? "]" : ")");
			}
		}
		setData(data);
	}

	public String[] convertToStringLimitValues(double[] limits, String precision) {
		String[] tableLimits = new String[limits.length];
		String rounded = "";
		int numDecimals;
		int indexE = precision.indexOf('E');
		if (indexE != -1) {
			numDecimals = Integer.parseInt(precision.substring(indexE + 2, indexE + 3));
		} else {
			int decimalPoint = precision.indexOf('.');
			int one = precision.indexOf('1');
			if (decimalPoint != -1 && one != -1) {
				numDecimals = one - decimalPoint;
			} else {
				numDecimals = 0;
			}
		}
		for (int i = 0; i < limits.length; i++) {
			if (limits[i] == Double.POSITIVE_INFINITY) {
				tableLimits[i] = INFINITY;
			} else if (limits[i] == Double.NEGATIVE_INFINITY) {
				tableLimits[i] = NEGATIVE_INFINITY;
			} else {
				rounded = Double.toString(limits[i]);
				// adding final zeros
				int roundedStringDecimalPlace = rounded.indexOf('.');
				if (roundedStringDecimalPlace == -1) {
					rounded += ".0";
				}
				roundedStringDecimalPlace = rounded.indexOf('.');
				int finalLength = roundedStringDecimalPlace + numDecimals + 1;
				if (finalLength <= rounded.length()) {
					rounded = rounded.substring(0, finalLength);
				} else {
					while (finalLength > rounded.length()) {
						rounded += "0";
					}
				}
				// rounded = rounded.replace(',', '.');
				tableLimits[i] = rounded;
			}
		}
		return tableLimits;
	}

	/**
	 * Returns the content of the table.
	 *
	 * @return the content of the table.
	 */
	@SuppressWarnings("rawtypes") @Override public Object[][] getData() {
		DiscretizeTableModel model = (DiscretizeTableModel) valuesTable.getModel();
		int columnCount = model.getColumnCount();
		int rowCount = model.getRowCount();
		int i = 0;
		int j = 0;
		Object[][] datatmp = new Object[rowCount][columnCount];
		Vector vectorData = model.getDataVector();
		Vector vectorRow = null;
		for (i = 0; i < rowCount; i++) {
			vectorRow = (Vector) vectorData.get(i);
			for (j = 0; j < columnCount; j++) {
				datatmp[i][j] = vectorRow.get(j);
			}
		}
		return datatmp;
	}

	/**
	 * Sets a new table model with new data.
	 *
	 * @param newData new data for the table without the key column.
	 */
	@Override public void setData(Object[][] newData) {
		if (newData != null) {
			data = fillDataKeys(newData);
			discretizeTableModel = new DiscretizeTableModel(data, columns);
			valuesTable.setModel(discretizeTableModel);
			valuesTable.getModel().addTableModelListener(this);
			this.defineTableLookAndFeel();
		}
	}

	/**
	 * This method takes a data object and creates a new column that content a
	 * row key. This key begins with the key prefix following a number that
	 * starts at 0.
	 *
	 * @param oldData data to add a key column.
	 * @return a data object with one more column that contains the keys.
	 */
	private Object[][] fillDataKeys(Object[][] oldData) {
		Object[][] newData = null;
		int i1 = 0; // aux int
		int i2 = 0; // aux int
		int l1 = 0; // num of rows
		int l2 = 0; // num of columns
		l1 = oldData.length;
		if (l1 > 0) {
			l2 = oldData[0].length + 1;
			newData = new Object[l1][l2];
			for (i1 = 0; i1 < l1; i1++) {
				newData[i1][0] = getKeyString(i1);
				for (i2 = 1; i2 < l2; i2++) {
					newData[i1][i2] = oldData[i1][i2 - 1];
				}
			}
			return newData;
		}
		return new Object[0][0];
	}

	/**
	 * Invoked when an action occurs.
	 *
	 * @param e event information.
	 */
	@Override public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource().equals(this.positiveInfinityButton)) {
			actionPerformedPositiveInfinityValue();
		} else if (e.getSource().equals(this.negativeInfinityButton)) {
			actionPerformedNegativeInfinityValue();
		}/*
		 * else if (e.getSource().equals(this.standardDomainButton)) {
		 * actionPerformedStandardDomain(); }
		 */
	}

	/**
	 * Invoked when the button 'add' is pressed.
	 */
	@Override protected void actionPerformedAddValue() {

		String option = (String) JOptionPane.showInputDialog(this, stringDatabase.getString("AddState.Text"),
				stringDatabase.getString("AddState.Text"), JOptionPane.QUESTION_MESSAGE, null, //no icon
				null, //no predefined values
				node.getVariable().getNewValidName()); // preset value in field

		if (option != null) {
			Variable variable = node.getVariable();
			int newIndex = 0;
			int newStateIndex = variable.getNumStates();
			NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.ADD, newStateIndex, option);
			try {
				node.getProbNet().doEdit(nodeStateEdit);
				// @ 2014/11/18. Issue 145.
				// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
				// Propagation of the domain in related variables in temporal models
				propagateNodeStateEditRelatedVariables(StateAction.ADD, newIndex, option);
				//
				if (variable.getVariableType() == VariableType.DISCRETIZED) {
					PartitionedInterval newPartitionedInterval = variable.getPartitionedInterval();
					setDataFromPartitionedInterval(newPartitionedInterval, variable.getStates());
				} else {
					getTableModel().insertRow(newIndex, new Object[] { getKeyString(newIndex), option });
				}
				valuesTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);
			} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
				JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
						stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Invoked when the button 'remove' is pressed.
	 */
	@Override protected void actionPerformedRemoveValue() {
		int selectedRow = valuesTable.getSelectedRow();
		removeState(selectedRow);
	}

	/**
	 * @param selectedRow
	 */
	protected void removeState(int selectedRow) {
		int rowCount = 0;
		NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.REMOVE, selectedRow, "");
		try {
			node.getProbNet().doEdit(nodeStateEdit);
			// @ 2014/11/18. Issue 145.
			// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
			// Propagation of the domain in related variables in temporal models
			propagateNodeStateEditRelatedVariables(StateAction.REMOVE, selectedRow, "");
			//
			cancelCellEditing();
			getTableModel().removeRow(selectedRow);
			rowCount = valuesTable.getRowCount();
			if (rowCount > 0) {
				if (selectedRow < rowCount) {
					valuesTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
					// update key column
					int auxSelectedRow = selectedRow;
					while (auxSelectedRow < rowCount) {
						getTableModel().setValueAt(getKeyString(auxSelectedRow), auxSelectedRow, 0);
						auxSelectedRow++;
					}
				} else {
					valuesTable.getSelectionModel().setSelectionInterval(selectedRow - 1, selectedRow - 1);
				}
				// after eliminating row check the lower limit, only if the variable type is discretized of numeric
				if (node.getVariable().getVariableType() == VariableType.DISCRETIZED
						|| node.getVariable().getVariableType() == VariableType.NUMERIC) {
					if (selectedRow > 0 && selectedRow < rowCount) {
						Object lowerBoundSymbol = valuesTable
								.getValueAt(selectedRow - 1, LOWER_BOUND_SYMBOL_COLUMN_INDEX);
						Object nextRowUpperBoundSymbol = (lowerBoundSymbol.equals("[")) ? ")" : "]";
						valuesTable.setValueAt(nextRowUpperBoundSymbol, selectedRow, UPPER_BOUND_SYMBOL_COLUMN_INDEX);
						Object nextRowLowerBound = valuesTable
								.getValueAt(selectedRow - 1, LOWER_BOUND_VALUE_COLUMN_INDEX);
						valuesTable.setValueAt(nextRowLowerBound, selectedRow, UPPER_BOUND_VALUE_COLUMN_INDEX);
					}
				}
			}
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
			// jTextFieldNodeName.setText( this.nodeProperties.getName() );
			// jTextFieldNodeName.requestFocus();
		}
		// TODO if variable is numeric or discretized it is necessary also to
		// change partitioned interval values
	}

	/**
	 * Invoked when the button 'up' is pressed.
	 */
	@Override protected void actionPerformedUpValue() {
		int selectedRow = valuesTable.getSelectedRow();
		Object swap = null;
		NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.UP, selectedRow, "");
		try {
			node.getProbNet().doEdit(nodeStateEdit);
			// @ 2014/11/18. Issue 145.
			// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
			// Propagation of the domain in related variables in temporal models
			propagateNodeStateEditRelatedVariables(StateAction.UP, selectedRow, "");
			//
			stopCellEditing();
			cancelCellEditing();
			swap = valuesTable.getValueAt(selectedRow, 1);
			valuesTable.setValueAt(valuesTable.getValueAt(selectedRow - 1, 1), selectedRow, 1);
			valuesTable.setValueAt(swap, selectedRow - 1, 1);
			valuesTable.getSelectionModel().setSelectionInterval(selectedRow - 1, selectedRow - 1);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Invoked when the button 'down' is pressed.
	 */
	@Override protected void actionPerformedDownValue() {
		int selectedRow = valuesTable.getSelectedRow();
		Object swap = null;
		NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.DOWN, selectedRow, "");
		try {
			node.getProbNet().doEdit(nodeStateEdit);
			// @ 2014/11/18. Issue 145.
			// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
			// Propagation of the domain in related variables in temporal models
			propagateNodeStateEditRelatedVariables(StateAction.DOWN, selectedRow, "");
			//
			stopCellEditing();
			cancelCellEditing();
			swap = valuesTable.getValueAt(selectedRow, 1);
			valuesTable.setValueAt(valuesTable.getValueAt(selectedRow + 1, 1), selectedRow, 1);
			valuesTable.setValueAt(swap, selectedRow + 1, 1);
			valuesTable.getSelectionModel().setSelectionInterval(selectedRow + 1, selectedRow + 1);
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
					stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Invoked when the button 'InfinitePositive' is pressed.
	 */
	protected void actionPerformedPositiveInfinityValue() {
		int selectedRow = valuesTable.getSelectedRow();
		int selectedColumn = valuesTable.getSelectedColumn();
		cancelCellEditing();
		double[] limits = node.getVariable().getPartitionedInterval().getLimits();
		boolean[] belongs = node.getVariable().getPartitionedInterval().getBelongsToLeftSide();
		limits[limits.length - 1] = Double.POSITIVE_INFINITY;
		belongs[limits.length - 1] = false;
		PartitionedInterval newPartitionedInterval = new PartitionedInterval(limits, belongs);
		PartitionedIntervalEdit partitionedIntervalEdit = new PartitionedIntervalEdit(node, newPartitionedInterval);
		try {
			node.getProbNet().doEdit(partitionedIntervalEdit);
		} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			e.printStackTrace();
		}
		valuesTable.setValueAt(INFINITY, selectedRow, selectedColumn);
	}

	/**
	 * Invoked when the button 'InfiniteNegative' is pressed.
	 */
	protected void actionPerformedNegativeInfinityValue() {
		int selectedRow = valuesTable.getSelectedRow();
		int selectedColumn = valuesTable.getSelectedColumn();
		cancelCellEditing();
		double[] limits = node.getVariable().getPartitionedInterval().getLimits();
		boolean[] belongs = node.getVariable().getPartitionedInterval().getBelongsToLeftSide();
		limits[0] = Double.NEGATIVE_INFINITY;
		belongs[0] = true;
		PartitionedInterval newPartitionedInterval = new PartitionedInterval(limits, belongs);
		PartitionedIntervalEdit partitionedIntervalEdit = new PartitionedIntervalEdit(node, newPartitionedInterval);
		try {
			node.getProbNet().doEdit(partitionedIntervalEdit);
		} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
			e.printStackTrace();
		}
		valuesTable.setValueAt(NEGATIVE_INFINITY, selectedRow, selectedColumn);
	}

	/**
	 * Returns a key represented by an index.
	 *
	 * @param index index of the key which will be returned
	 * @return the string that content the key.
	 */
	private String getKeyString(int index) {
		return keyPrefix + index;
	}

	public void tableChanged(TableModelEvent tableEvent) {
		int column = tableEvent.getColumn();
		int row = tableEvent.getLastRow();
		// We save the index of the modified state
		int indexState = node.getVariable().getNumStates() - row - 1;
		tableEvent.getSource();

		if (tableEvent.getType() == TableModelEvent.UPDATE) {
			Object value = ((DiscretizeTableModel) tableEvent.getSource()).getValueAt(row, column);
			boolean lower = column == LOWER_BOUND_VALUE_COLUMN_INDEX;
			if (value instanceof String && column == INTERVAL_NAME_COLUMN_INDEX) {
				String newName = value.toString();
				// We only execute the Edit if the name really changed
				if (!newName.equals(node.getVariable().getStateName(indexState))) {
					NodeStateEdit nodeStateEdit = new NodeStateEdit(node, StateAction.RENAME, row, newName);
					try {
						node.getProbNet().doEdit(nodeStateEdit);
						// @ 2014/11/18. Issue 145.
						// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
						// Propagation of the domain in related variables in temporal models
						propagateNodeStateEditRelatedVariables(StateAction.RENAME, row, newName);
						//
					} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
						JOptionPane.showMessageDialog(this, stringDatabase.getString(e.getMessage()),
								stringDatabase.getString(e.getMessage()), JOptionPane.ERROR_MESSAGE);
						// If an error occurred or a constraint is broken
						// we restore the old name of the edited state
						String oldState = node.getVariable().getStateName(indexState);
						valuesTable.setValueAt(oldState, row, column);

					}
				}
			}
			if (value == INFINITY && column == UPPER_BOUND_VALUE_COLUMN_INDEX) {
				valuesTable.setValueAt(INFINITY, row, column);
				String upperBound = (String) valuesTable.getValueAt(row, column + 1);
				if (upperBound.equals("]")) {
					valuesTable.setValueAt(")", row, column + 1);
				}
			} else if (value == NEGATIVE_INFINITY && column == LOWER_BOUND_VALUE_COLUMN_INDEX) {
				valuesTable.setValueAt(NEGATIVE_INFINITY, row, column);
				String lowerBound = (String) valuesTable.getValueAt(row, column - 1);
				if (lowerBound.equals("[")) {
					valuesTable.setValueAt("(", row, column - 1);
				}
			} else if (value instanceof Double) {
				Variable variable = node.getVariable();
				double newValue = (Double) value;
				// setting precision to the new value according with the
				// precision value introduced by the user
				double precision = variable.getPrecision();
				double roundedValue = Util.roundWithPrecision(newValue, Double.toString(precision));
				double[] currentLimits = variable.getPartitionedInterval().getLimits();
				int numLimits = currentLimits.length;
				boolean[] currentBelongsToLeft = variable.getPartitionedInterval().getBelongsToLeftSide();
				int limitsIndex = (lower) ? numLimits - row - 2 : numLimits - row - 1;
				// posterior limits
				int i = limitsIndex;
				currentLimits[i] = roundedValue;
				while (i + 1 < currentLimits.length && currentLimits[i] >= currentLimits[i + 1]) {
					if (!currentBelongsToLeft[i] && currentBelongsToLeft[i + 1]) {
						currentLimits[i + 1] = currentLimits[i];
					} else {
						if (i + 1 == currentLimits.length - 1) {
							currentLimits[i + 1] = Double.POSITIVE_INFINITY;
							break;
						} else
							currentLimits[i + 1] = currentLimits[i] + precision;
					}
					i++;
				}
				// previous limits
				int k = limitsIndex;
				while (k - 1 >= 0 && currentLimits[k] <= currentLimits[k - 1]) {
					if (currentBelongsToLeft[k] && !currentBelongsToLeft[k - 1]) {
						currentLimits[k - 1] = currentLimits[k];
					} else {
						if (k - 1 == 0) {
							currentLimits[k - 1] = Double.NEGATIVE_INFINITY;
							break;
						} else
							currentLimits[k - 1] = currentLimits[k] - precision;
					}
					k--;
				}
				for (int m = 0; m < currentLimits.length; m++) {
					if (currentLimits[m] != Double.POSITIVE_INFINITY && currentLimits[m] != Double.NEGATIVE_INFINITY) {
						currentLimits[m] = Util.roundWithPrecision(currentLimits[m], Double.toString(precision));
					}
				}
				PartitionedInterval newPartitionedInterval = new PartitionedInterval(currentLimits,
						currentBelongsToLeft);
				PartitionedIntervalEdit partitionedIntervalEdit = new PartitionedIntervalEdit(node,
						newPartitionedInterval);
				try {
					node.getProbNet().doEdit(partitionedIntervalEdit);
				} catch (DoEditException | ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException e) {
					e.printStackTrace();
				}
				setDataFromPartitionedInterval(variable.getPartitionedInterval(), variable.getStates());
			}
		}
	}

	public void setEnablePanelButton(boolean b) {
		if (b) {
			addValueButton.setEnabled(b);
		} else {
			addValueButton.setEnabled(b);
			upValueButton.setEnabled(b);
			downValueButton.setEnabled(b);
			removeValueButton.setEnabled(b);
		}
	}

	public void setVisibleButtonPanel(boolean b) {
		getButtonPanel().setVisible(b);
	}

	public void mouseClicked(MouseEvent e) {
		int row = valuesTable.rowAtPoint(e.getPoint());
		int column = valuesTable.columnAtPoint(e.getPoint());
		Variable variable = node.getVariable();
		if (variable.getVariableType() == VariableType.NUMERIC
				|| variable.getVariableType() == VariableType.DISCRETIZED) {
			if (column == LOWER_BOUND_SYMBOL_COLUMN_INDEX || column == UPPER_BOUND_SYMBOL_COLUMN_INDEX) {
				changeLimitIntervalDiscretize(row, column);
			} else if (column == LOWER_BOUND_VALUE_COLUMN_INDEX || column == UPPER_BOUND_VALUE_COLUMN_INDEX) {
				// infinity buttons management
				int numIntervals = variable.getPartitionedInterval().getNumSubintervals();

				boolean showNegInfinity = (column == LOWER_BOUND_VALUE_COLUMN_INDEX) && (
						isUpMonotony() && row == numIntervals - 1 || !isUpMonotony() && row == 0
				);
				boolean showInfinity = (column == UPPER_BOUND_VALUE_COLUMN_INDEX) && (
						isUpMonotony() && row == 0 || !isUpMonotony() && row == numIntervals - 1
				);
				getNegativeInfinityButton().setVisible(showNegInfinity);
				getNegativeInfinityButton().setEnabled(showNegInfinity);
				getPositiveInfinityButton().setVisible(showInfinity);
				getPositiveInfinityButton().setEnabled(showInfinity);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	// @ 2014/11/18. Issue 145.
	// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
	// Propagation of the domain in related variables in temporal models
	private void propagateNodeStateEditRelatedVariables(StateAction stateAction, int selectedRow, String option) {
		// First we get the nodes in the same time slide as the node currently being edited
		List<Node> nodeRelatedNodes = TemporalNetOperations.getRelatedNodesOtherTimeSlices(node);
		// We create a variable to store the edit of the related node
		NodeStateEdit nodeStateEdit;
		try {
			// We iterate the related nodes, if any
			if (nodeRelatedNodes != null) {
				if (nodeRelatedNodes.size() > 0) {
					for (Node relatedNode : nodeRelatedNodes) {
						// we create the edit for the realted node
						nodeStateEdit = new NodeStateEdit(relatedNode, stateAction, selectedRow, option);
						// and we perform the edit
						relatedNode.getProbNet().doEdit(nodeStateEdit);
					}
				}
			}
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}
	}

	// @ 2014/11/18. Issue 145.
	// https://bitbucket.org/cisiad/org.openmarkov.issues/issue/145/domains-in-mpads-related-variables
	// Propagation of the domain in related variables in temporal models
	private void propagateNodePartitionedIntervalEditRelatedVariables(StateAction stateAction, int selectedRow,
			boolean option) {
		// First we get the nodes in the same time slide as the node currently being edited
		List<Node> nodeRelatedNodes = TemporalNetOperations.getRelatedNodesOtherTimeSlices(node);
		// We create a variable to store the edit of the related node
		NodePartitionedIntervalEdit nodePartitionedIntervalEdit;
		try {
			// We iterate the related nodes, if any
			if (nodeRelatedNodes != null) {
				if (nodeRelatedNodes.size() > 0) {
					for (Node relatedNode : nodeRelatedNodes) {
						// we create the edit for the realted node
						nodePartitionedIntervalEdit = new NodePartitionedIntervalEdit(relatedNode, stateAction,
								selectedRow, option);
						// and we perform the edit
						relatedNode.getProbNet().doEdit(nodePartitionedIntervalEdit);
					}
				}
			}
		} catch (ConstraintViolationException | NonProjectablePotentialException | WrongCriterionException | DoEditException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Class to manage Discretize Render Table in columns "()" and "[]"
	 *
	 * @author Alberto Ruiz
	 */
	public class DiscretizeComboBoxRenderer extends JComboBox<String> implements TableCellRenderer {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public DiscretizeComboBoxRenderer(String[] items) {
			super(items);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			// Select the current value
			setSelectedItem(value);
			return this;
		}
	}

}
