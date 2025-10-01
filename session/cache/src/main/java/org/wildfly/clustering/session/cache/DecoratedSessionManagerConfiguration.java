/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.time.Duration;
import java.util.function.Consumer;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManagerConfiguration;

/**
 * A session manager configuration decorator.
 * @author Paul Ferraro
 * @param <C> the servlet context type
 */
public class DecoratedSessionManagerConfiguration<C> implements SessionManagerConfiguration<C> {

	private final SessionManagerConfiguration<C> configuration;

	/**
	 * Creates a session manager configuration decorator.
	 * @param configuration the decorated configuration
	 */
	public DecoratedSessionManagerConfiguration(SessionManagerConfiguration<C> configuration) {
		this.configuration = configuration;
	}

	@Override
	public Consumer<ImmutableSession> getExpirationListener() {
		return this.configuration.getExpirationListener();
	}

	@Override
	public Duration getTimeout() {
		return this.configuration.getTimeout();
	}

	@Override
	public C getContext() {
		return this.configuration.getContext();
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.configuration.getIdentifierFactory();
	}
}
