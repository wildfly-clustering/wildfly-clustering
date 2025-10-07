/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * An abstract primary owner command.
 * @author Paul Ferraro
 * @param <K> the scheduled entry key type
 * @param <V> the scheduled entry value type
 * @param <R> the command return type
 */
public abstract class AbstractPrimaryOwnerCommand<K, V, R> implements PrimaryOwnerCommand<K, V, R> {

	private final K key;

	AbstractPrimaryOwnerCommand(K key) {
		this.key = key;
	}

	@Override
	public K getKey() {
		return this.key;
	}

	Object getParameter() {
		return this.key;
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", this.getClass().getSimpleName(), this.getParameter());
	}
}
