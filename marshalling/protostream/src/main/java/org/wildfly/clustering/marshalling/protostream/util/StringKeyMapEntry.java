/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.util.AbstractMap;
import java.util.Map;

/**
 * A map entry whose key is a string.
 * @author Paul Ferraro
 * @param <V> the map entry value type
 */
public class StringKeyMapEntry<V> extends AbstractMap.SimpleEntry<String, V> {
	private static final long serialVersionUID = 6746886011081684846L;

	public StringKeyMapEntry(Map.Entry<String, V> entry) {
		super(entry);
	}

	public StringKeyMapEntry(String key, V value) {
		super(key, value);
	}
}
