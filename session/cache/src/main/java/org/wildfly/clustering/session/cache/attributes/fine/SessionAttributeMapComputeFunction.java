/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.util.Map;

import org.wildfly.clustering.cache.function.MapComputeFunction;

/**
 * A session attribute map compute function.
 * @param <V> the session attribute value type
 * @author Paul Ferraro
 */
public class SessionAttributeMapComputeFunction<V> extends MapComputeFunction<String, V> {
	/**
	 * Creates a session attribute map compute function.
	 * @param operand a map of sessions to add, update, or remove.
	 */
	public SessionAttributeMapComputeFunction(Map<String, V> operand) {
		super(operand);
	}
}
