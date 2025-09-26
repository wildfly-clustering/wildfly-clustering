/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.util.AbstractMap;
import java.util.Map;

/**
 * A string-based map entry.
 * @author Paul Ferraro
 */
public class Property extends AbstractMap.SimpleEntry<String, String> {
	private static final long serialVersionUID = -8779521775890292166L;

	/**
	 * Creates a property from the specified map entry.
	 * @param entry a map entry
	 */
	public Property(Map.Entry<String, String> entry) {
		super(entry);
	}

	/**
	 * Creates a property from the specified key and value.
	 * @param key the entry key
	 * @param value the entry value
	 */
	public Property(String key, String value) {
		super(key, value);
	}
}
