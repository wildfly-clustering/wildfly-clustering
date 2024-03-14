/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.function.Consumer;

/**
 * @param <T> test subject type
 * @author Paul Ferraro
 */
public interface Tester<T> extends Consumer<T> {

	void reject(T value);

	<E extends Throwable> void reject(T value, Class<E> expected);
}
