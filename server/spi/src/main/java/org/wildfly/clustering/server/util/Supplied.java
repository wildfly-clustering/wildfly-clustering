/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * A supplied value.
 * @author Paul Ferraro
 * @param <T> the supplied value type
 */
public interface Supplied<T> {
	/** A simple supplied value */
	Supplied<?> SIMPLE = new Supplied<>() {
		@Override
		public Object get(Supplier<Object> supplier) {
			return supplier.get();
		}
	};

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
				T value = reference.get();
				return (value != null) ? value : reference.updateAndGet(UnaryOperator.when(Objects::nonNull, UnaryOperator.identity(), UnaryOperator.of(Consumer.of(), factory)));
			}
		};
	}

	/**
	 * A simple supplied value.
	 * @param <T> the supplied value type
	 * @return the supplied value
	 */
	@SuppressWarnings("unchecked")
	static <T> Supplied<T> simple() {
		return (Supplied<T>) SIMPLE;
	}
}
