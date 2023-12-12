/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.nio.ByteBuffer;

import org.wildfly.clustering.marshalling.Marshaller;

/**
 * @author Paul Ferraro
 */
public interface IdentifierMarshallerProvider {
	Marshaller<String, ByteBuffer> getMarshaller();
}
