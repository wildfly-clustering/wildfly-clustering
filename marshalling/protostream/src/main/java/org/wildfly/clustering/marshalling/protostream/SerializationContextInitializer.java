/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * @author Paul Ferraro
 */
public interface SerializationContextInitializer {

	void registerSchema(SerializationContext context);

	void registerMarshallers(SerializationContext context);
}
