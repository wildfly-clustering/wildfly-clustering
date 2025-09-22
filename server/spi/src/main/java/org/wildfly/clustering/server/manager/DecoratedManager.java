/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.service.DecoratedService;
import org.wildfly.clustering.server.service.Service;

/**
 * A {@link Service} decorator.
 * @author Paul Ferraro
 * @param <I> the identifer type
 */
public class DecoratedManager<I> extends DecoratedService implements Manager<I> {

	private final Manager<I> manager;

	/**
	 * Creates a decorator of the specified manager.
	 * @param manager the decorated manager
	 */
	public DecoratedManager(Manager<I> manager) {
		this(manager, manager);
	}

	/**
	 * Creates a decorator of the specified manager with an alternate service.
	 * @param manager the decorated manager
	 * @param service the alternate decorated service
	 */
	protected DecoratedManager(Manager<I> manager, Service service) {
		super(service);
		this.manager = manager;
	}

	@Override
	public Supplier<I> getIdentifierFactory() {
		return this.manager.getIdentifierFactory();
	}

	@Override
	public Supplier<Batch> getBatchFactory() {
		return this.manager.getBatchFactory();
	}
}
