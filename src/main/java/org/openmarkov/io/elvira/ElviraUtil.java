/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;

/**
 * @author marias
 */
public class ElviraUtil {

	/**  */
	public static void swapNameAndTitle(ProbNet probNet) {
		List<Node> nodes = probNet.getNodes();
		for (Node node : nodes) {
			String title = node.additionalProperties.get("Title");
			if ((title != null) && (title.length() > 0)) {
				Variable variable = node.getVariable();
				String variableName = variable.getName();
				variable.setName(title);
				node.additionalProperties.put("Title", variableName);
			}
		}
	}

	/**
	 * Puts a property A that is an array as a set of additionalProperties with
	 * names A[0], A[1],...
	 *
	 * @param key    . <code>String</code>
	 * @param values . <code>List</code> of <code>String</code>
	 */
	public static void putPropertyArray(Map<String, String> properties, String key, List<String> values) {
		if (values != null) {
			int numProperties = values.size();
			for (int i = 0; i < numProperties; i++) {
				properties.put(key + "[" + i + "]", values.get(i));
			}
		}
	}

	/**
	 * Gets a multi-valued property as a List.
	 *
	 * @param key . <code>String</code>
	 * @return <code>List</code> of <code>String</code>
	 */
	public static List<String> getPropertyArray(Map<String, String> properties, String key) {
		List<String> values = new ArrayList<String>();
		int i = 0;
		String value = null;
		do {
			String extendedKey = key + "[" + i++ + "]";
			value = properties.get(extendedKey);
			if (value != null) {
				values.add(value);
			}
		} while (value != null);
		if (values.size() == 0) { // property does not exist
			values = null;
		}
		return values;
	}

}
