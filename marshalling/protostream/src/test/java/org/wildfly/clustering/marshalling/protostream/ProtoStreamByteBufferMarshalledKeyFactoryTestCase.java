/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.wildfly.clustering.marshalling.ByteBufferMarshalledKeyFactoryTestCase;

/**
 * @author Paul Ferraro
 */
public class ProtoStreamByteBufferMarshalledKeyFactoryTestCase extends ByteBufferMarshalledKeyFactoryTestCase {

	public ProtoStreamByteBufferMarshalledKeyFactoryTestCase() {
		super(new ProtoStreamTesterFactory().get());
	}
}
