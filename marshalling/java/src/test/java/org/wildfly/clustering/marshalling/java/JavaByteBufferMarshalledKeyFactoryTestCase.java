/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractByteBufferMarshalledKeyFactoryTestCase;

/**
 * Java serialization variant of {@link AbstractByteBufferMarshalledKeyFactoryTestCase}.
 * @author Paul Ferraro
 */
public class JavaByteBufferMarshalledKeyFactoryTestCase extends AbstractByteBufferMarshalledKeyFactoryTestCase {

	public JavaByteBufferMarshalledKeyFactoryTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
