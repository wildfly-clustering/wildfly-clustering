/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.nio.ByteBuffer;

/**
 * A service provider interface for marshaller providers.
 * @author Paul Ferraro
 */
public interface MarshallerProvider<T> {

	/**
	 * Returns the class for which this implementation provides a marshaller.
	 * @return the class for which this implementation provides a marshaller.
	 */
	Class<T> getProvidedClass();

	@SuppressWarnings("unchecked")
	default <U> MarshallerProvider<U> asSubclass(Class<U> targetClass) {
		if (!targetClass.isAssignableFrom(this.getProvidedClass())) {
			throw new ClassCastException(this.getClass().getName());
		}
		return (MarshallerProvider<U>) this;
	}

	/**
	 * Returns a marshaller suitable for marshalling an object of the specified class
	 * @param <T> the target class type
	 * @param targetClass a target class
	 * @return an optional marshaller of for the specified class
	 */
	Marshaller<T, ByteBuffer> getMarshaller();
}
