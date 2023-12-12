/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.user;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Cache entry that stores persistent and transient user identity..
 * @author Paul Ferraro
 * @param <C> the persistent user context type
 * @param <L> the transient user context type
 */
public class UserContextEntry<C, L> implements UserContext<C, L> {

	private final C context;
	private final AtomicReference<L> transientContext = new AtomicReference<>();

	public UserContextEntry(C context) {
		this.context = context;
	}

	@Override
	public C getContext() {
		return this.context;
	}

	@Override
	public L getContext(Supplier<L> factory) {
		return this.transientContext.updateAndGet(new UnaryOperator<>() {
			@Override
			public L apply(L context) {
				return (context != null) ? context : factory.get();
			}
		});
	}
}
