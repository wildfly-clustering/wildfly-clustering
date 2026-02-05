/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractByteBufferMarshalledValueFactoryTestCase;

/**
 * Java serialization variant of {@link AbstractByteBufferMarshalledValueFactoryTestCase}.
 * @author Paul Ferraro
 */
public class JavaByteBufferMarshalledValueFactoryTestCase extends AbstractByteBufferMarshalledValueFactoryTestCase {

	public JavaByteBufferMarshalledValueFactoryTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
