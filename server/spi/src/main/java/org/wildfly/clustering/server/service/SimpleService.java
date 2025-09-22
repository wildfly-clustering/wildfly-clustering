/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.service;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * A simple service facade to an AutoCloseable factory.
 * @author Paul Ferraro
 * @param <T> the operating type
 */
public class SimpleService<T extends AutoCloseable> implements Service {
	private final UnaryOperator<T> update;
	private final AtomicReference<T> reference;

	public SimpleService(Supplier<T> factory) {
		this(factory, new AtomicReference<>());
	}

	public SimpleService(Supplier<T> factory, AtomicReference<T> reference) {
		this.update = UnaryOperator.<T>identity().orDefault(Objects::nonNull, factory);
		this.reference = reference;
	}

	@Override
	public boolean isStarted() {
		return this.reference.get() != null;
	}

	@Override
	public void start() {
		Consumer.close().accept(this.reference.getAndUpdate(this.update));
	}

	@Override
	public void stop() {
		Consumer.close().accept(this.reference.getAndSet(null));
	}
}
