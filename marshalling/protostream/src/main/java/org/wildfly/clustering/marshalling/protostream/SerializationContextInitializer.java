/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * A serialization context initializer.
 * @author Paul Ferraro
 */
public interface SerializationContextInitializer {
	/**
	 * Initialises the specified context by registering a schema and associated marshallers.
	 * @param context the context to initialise
	 */
	default void initialize(SerializationContext context) {
		this.registerSchema(context);
		this.registerMarshallers(context);
	}

	/**
	 * Registers a protobuf schema.
	 * @param context the context into which the schema should be registered.
	 */
	void registerSchema(SerializationContext context);

	/**
	 * Registers a number of marshallers.
	 * @param context the context into which marshallers should be registered.
	 */
	void registerMarshallers(SerializationContext context);
}
