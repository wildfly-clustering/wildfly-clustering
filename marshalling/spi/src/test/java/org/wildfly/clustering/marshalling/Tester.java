/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Test interface for marshalling validation.
 * @param <T> test subject type
 * @author Paul Ferraro
 */
public interface Tester<T> extends Consumer<T>, UnaryOperator<T> {

	@Override
	default void accept(T value) {
		this.apply(value);
	}

	/**
	 * Validates that the specified value is rejected by the tester.
	 * @param value an unmarshallable value
	 */
	void reject(T value);

	/**
	 * Validates that an attempt to marshal the specified value throws the specified exception type.
	 * @param <E> the expected exception type
	 * @param value an unmarshallable value
	 * @param expected the expected exception type
	 */
	<E extends Throwable> void reject(T value, Class<E> expected);
}
