/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import org.wildfly.clustering.function.Predicate;

/**
 * Factory for creating marshalled values.
 * @param <C> the marshalling context type
 * @author Paul Ferraro
 */
public interface MarshalledValueFactory<C> extends Predicate<Object> {
	/**
	 * Creates a new marshalled value from the specified object.
	 * @param <T> the value type
	 * @param object a value to be marshalled
	 * @return a marshalled value
	 */
	<T> MarshalledValue<T, C> createMarshalledValue(T object);

	/**
	 * The marshalling context applied to marshalled values created by this factory.
	 * @return the marshalling context
	 */
	C getMarshallingContext();
}
