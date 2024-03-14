/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.nio.ByteBuffer;

/**
 * Encapsulates the marshalling of an object. This allows us to run a set of marshalling tests across different marshallers.
 * @param <T> marshaller target type
 * @author Paul Ferraro
 */
public interface TestMarshaller<T> extends Marshaller<T, ByteBuffer> {
}
