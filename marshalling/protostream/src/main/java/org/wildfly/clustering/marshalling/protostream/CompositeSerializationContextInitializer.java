/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.EnumSet;
import java.util.List;

/**
 * A {@link SerializationContextInitializer} decorator that registers schemas and marshallers from multiple {@link SerializationContextInitializer} instances.
 * @author Paul Ferraro
 */
public class CompositeSerializationContextInitializer implements SerializationContextInitializer {

	private final Iterable<? extends SerializationContextInitializer> initializers;

	/**
	 * Creates a composite serialization context initializer.
	 * @param initializer1 a serialization context initializer
	 * @param initializer2 another serialization context initializer.
	 */
	public CompositeSerializationContextInitializer(SerializationContextInitializer initializer1, SerializationContextInitializer initializer2) {
		this(List.of(initializer1, initializer2));
	}

	/**
	 * Creates a composite serialization context initializer.
	 * @param initializers a number of serialization context initializers
	 */
	public CompositeSerializationContextInitializer(SerializationContextInitializer... initializers) {
		this(List.of(initializers));
	}

	/**
	 * Creates a composite serialization context initializer.
	 * @param <E> the enum type
	 * @param enumClass the class of an enumeration of serialization context initializers
	 */
	public <E extends Enum<E> & SerializationContextInitializer> CompositeSerializationContextInitializer(Class<E> enumClass) {
		this(EnumSet.allOf(enumClass));
	}

	/**
	 * Creates a composite serialization context initializer.
	 * @param initializers a number of serialization context initializers
	 */
	public CompositeSerializationContextInitializer(Iterable<? extends SerializationContextInitializer> initializers) {
		this.initializers = initializers;
	}

	@Override
	public void registerSchema(SerializationContext context) {
		for (SerializationContextInitializer initializer : this.initializers) {
			initializer.registerSchema(context);
		}
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		for (SerializationContextInitializer initializer : this.initializers) {
			initializer.registerMarshallers(context);
		}
	}
}
