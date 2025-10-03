/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.manager;

import java.util.function.Supplier;

import org.wildfly.clustering.server.manager.IdentifierFactoryService;
import org.wildfly.clustering.server.service.SimpleService;

/**
 * Simple {@link IdentifierFactoryService} that delegates to a supplier.
 * @param <I> the identifier type
 * @author Paul Ferraro
 */
public class SimpleIdentifierFactoryService<I> extends SimpleService implements IdentifierFactoryService<I> {

	private final Supplier<I> factory;

	/**
	 * Creates an identifier factory service based on the specified factory.
	 * @param factory an identifier factory.
	 */
	public SimpleIdentifierFactoryService(Supplier<I> factory) {
		this.factory = factory;
	}

	@Override
	public I get() {
		return this.factory.get();
	}
}
