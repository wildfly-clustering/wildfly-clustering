/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.Function;

/**
 * Consumer extension supporting map operations.
 * @author Paul Ferraro
 * @param <T> the accepted type
 */
public interface Consumer<T> extends java.util.function.Consumer<T> {

	@Override
	default Consumer<T> andThen(java.util.function.Consumer<? super T> after) {
		return new Consumer<>() {
			@Override
			public void accept(T value) {
				Consumer.this.accept(value);
				after.accept(value);
			}
		};
	}

	default <V> Consumer<V> map(Function<V, T> mapper) {
		return new Consumer<>() {
			@Override
			public void accept(V value) {
				Consumer.this.accept(mapper.apply(value));
			}
		};
	}
}
