/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import java.io.IOException;

/**
 * Marshaller that stores attribute values using marshalled values.
 * @param <V> the marshalled value type
 * @param <C> the marshalling context type
 * @author Paul Ferraro
 */
public class MarshalledValueMarshaller<V, C> implements Marshaller<V, MarshalledValue<V, C>> {
	private final MarshalledValueFactory<C> factory;

	/**
	 * Constructs a new marshaller using the specified marshalled value factory.
	 * @param factory a marshalled value factory
	 */
	public MarshalledValueMarshaller(MarshalledValueFactory<C> factory) {
		this.factory = factory;
	}

	@Override
	public V read(MarshalledValue<V, C> value) throws IOException {
		if (value == null) return null;
		return value.get(this.factory.getMarshallingContext());
	}

	@Override
	public MarshalledValue<V, C> write(V object) {
		if (object == null) return null;
		return this.factory.createMarshalledValue(object);
	}

	@Override
	public boolean test(Object object) {
		return this.factory.test(object);
	}
}
