/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.plugin.PluginLoader;
import org.openmarkov.plugin.service.FilterIF;
import org.openmarkov.plugin.service.PluginLoaderIF;

import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PotentialPanelManager {
	/**
	 * Singleton instance
	 */
	private static PotentialPanelManager instance = null;

	private PluginLoaderIF pluginsLoader;
	private HashMap<String, Class<? extends PotentialPanel>> potentialPanelClasses;

	/**
	 * Constructor for PotentialPanelManager.
	 */
	@SuppressWarnings("unchecked") private PotentialPanelManager() {
		super();
		this.pluginsLoader = new PluginLoader();
		potentialPanelClasses = new HashMap<String, Class<? extends PotentialPanel>>();
		for (Class<?> plugin : findAllPotentials()) {
			PotentialPanelPlugin lAnnotation = plugin.getAnnotation(PotentialPanelPlugin.class);
			if (PotentialPanel.class.isAssignableFrom(plugin)) {
				potentialPanelClasses.put(lAnnotation.potentialType(), (Class<? extends PotentialPanel>) plugin);
			} else {
				throw new AnnotationFormatError(
						"PotentialPanelPlugin annotation must be in a class that extends PotentialPanel");
			}
		}
	}

	public static PotentialPanelManager getInstance() {
		if (instance == null) {
			instance = new PotentialPanelManager();
		}
		return instance;
	}

	/**
	 * Returns a potential panel by name.
	 *
	 * @param potentialType the potential's name.
	 * @return a new Potential instance given the parameters.
	 */
	public final PotentialPanel getPotentialPanel(String potentialType, Node node) {
		PotentialPanel instance = null;
		if (potentialPanelClasses.get(potentialType) != null) {
			try {
				Constructor<? extends PotentialPanel> constructor = potentialPanelClasses.get(potentialType)
						.getConstructor(Node.class);
				instance = constructor.newInstance(node);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (instance == null) {
			instance = new EmptyPotentialPanel(node);
		}
		return instance;
	}

	/**
	 * Returns a potential panel by name or family.
	 *
	 * @param potentialType   the potential's name.
	 * @param potentialFamily the potential's family.
	 * @return a new Potential instance given the parameters.
	 */
	public final PotentialPanel getPotentialPanel(String potentialType, String potentialFamily, Node node) {
		PotentialPanel instance = null;
		if (potentialPanelClasses.get(potentialFamily) != null) {
			try {
				Constructor<? extends PotentialPanel> constructor = potentialPanelClasses.get(potentialFamily)
						.getConstructor(Node.class);
				instance = constructor.newInstance(node);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (potentialPanelClasses.get(potentialType) != null) {
			try {
				Constructor<? extends PotentialPanel> constructor = potentialPanelClasses.get(potentialType)
						.getConstructor(Node.class);
				instance = constructor.newInstance(node);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (instance == null) {
			instance = new EmptyPotentialPanel(node);
		}
		return instance;
	}

	/**
	 * Returns all potential panels' names.
	 *
	 * @return a list of potential panels' names.
	 */
	public final Set<String> getAllPotentialsNames() {
		return potentialPanelClasses.keySet();
	}

	/**
	 * Finds all learning algorithms.
	 *
	 * @return a list of learning algorithms.
	 */
	private final List<Class<?>> findAllPotentials() {
		try {
			FilterIF filter = org.openmarkov.plugin.Filter.filter().toBeAnnotatedBy(PotentialPanelPlugin.class);
			return pluginsLoader.loadAllPlugins(filter);
		} catch (Exception e) {
		}
		return null;
	}
}
