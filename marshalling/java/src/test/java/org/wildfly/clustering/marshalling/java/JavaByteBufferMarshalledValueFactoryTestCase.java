/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.ByteBufferMarshalledValueFactoryTestCase;

/**
 * Unit tests for {@link ByteBufferMarshalledValue}.
 *
 * @author Brian Stansberry
 * @author Paul Ferraro
 */
public class JavaByteBufferMarshalledValueFactoryTestCase  extends ByteBufferMarshalledValueFactoryTestCase {

	public JavaByteBufferMarshalledValueFactoryTestCase() {
		super(new JavaByteBufferMarshaller());
	}
}
