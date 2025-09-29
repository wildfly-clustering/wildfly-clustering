/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.nio.ByteBuffer;

import org.wildfly.clustering.marshalling.Marshaller;

/**
 * Provides a session identifier marshaller.
 * @author Paul Ferraro
 */
public interface IdentifierMarshallerProvider {
	/**
	 * Returns the marshaller used to marshal session identifiers.
	 * @return the marshaller used to marshal session identifiers.
	 */
	Marshaller<String, ByteBuffer> getMarshaller();
}
