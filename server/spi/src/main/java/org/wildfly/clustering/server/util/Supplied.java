/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.util;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A supplied value.
 * @author Paul Ferraro
 * @param <T> the supplied value type
 */
public interface Supplied<T> {

	/**
	 * Returns the supplied value, created from the specified factory if necessary.
	 * @param factory a value supplier
	 * @return the supplied value
	 */
	T get(Supplier<T> factory);

	/***
	 * A cached supplied value.
	 * @param <T> the supplied value type
	 * @return the supplied or cached value
	 */
	static <T> Supplied<T> cached() {
		AtomicReference<T> reference = new AtomicReference<>();
		return new Supplied<>() {
			@Override
			public T get(Supplier<T> factory) {
				return reference.updateAndGet(context -> Optional.ofNullable(context).orElseGet(factory));
			}
		};
	}

	/**
	 * A simple supplied value.
	 * @param <T>
	 * @return the supplied value
	 */
	static <T> Supplied<T> simple() {
		return new Supplied<>() {
			@Override
			public T get(Supplier<T> factory) {
				return factory.get();
			}
		};
	}
}
