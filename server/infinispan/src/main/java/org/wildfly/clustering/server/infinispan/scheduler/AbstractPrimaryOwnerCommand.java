/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * An abstract primary owner command.
 * @author Paul Ferraro
 * @param <I> the scheduled entry identifier type
 * @param <M> the scheduled entry metadata type
 * @param <R> the command return type
 */
public abstract class AbstractPrimaryOwnerCommand<I, M, R> implements PrimaryOwnerCommand<I, M, R> {

	private final I id;

	AbstractPrimaryOwnerCommand(I id) {
		this.id = id;
	}

	@Override
	public I getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", this.getClass().getSimpleName(), this.id);
	}
}
