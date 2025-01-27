/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.util.AbstractMap;
import java.util.Map;

/**
 * @author Paul Ferraro
 */
public class Property extends AbstractMap.SimpleEntry<String, String> {
	private static final long serialVersionUID = -8779521775890292166L;

	public Property(Map.Entry<String, String> entry) {
		super(entry);
	}

	public Property(String key, String value) {
		super(key, value);
	}
}
