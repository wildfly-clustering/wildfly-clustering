/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.BiFunction;

/**
 * Generic function for use with {@link java.util.Map#compute(Object, BiFunction)} operations using {@link Remappable} values.
 * @author Paul Ferraro
 * @param <V> the cache value type
 * @param <O> the operand type
 */
public class RemappingFunction<V extends Remappable<V, O>, O> implements BiFunction<Object, V, V>, Operation<O> {

	private final O operand;

	/**
	 * Creates a new remapping function.
	 * @param operand the operation operand.
	 */
	public RemappingFunction(O operand) {
		this.operand = operand;
	}

	@Override
	public O getOperand() {
		return this.operand;
	}

	@Override
	public V apply(Object key, V value) {
		return (value != null) ? value.remap(this.operand) : null;
	}
}
