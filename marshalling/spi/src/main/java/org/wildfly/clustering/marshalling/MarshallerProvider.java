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
	 * Returns a marshaller suitable for the provided class
	 * @return a marshaller suitable for the provided class
	 */
	Marshaller<T, ByteBuffer> getMarshaller();
}
