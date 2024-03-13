/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.ByteBufferMarshalledKeyFactoryTestCase;

/**
 * Java serialization variant of {@link ByteBufferMarshalledKeyFactoryTestCase}.
 * @author Paul Ferraro
 */
public class JavaByteBufferMarshalledKeyFactoryTestCase extends ByteBufferMarshalledKeyFactoryTestCase {

	public JavaByteBufferMarshalledKeyFactoryTestCase() {
		super(JavaTesterFactory.INSTANCE.getMarshaller());
	}
}
