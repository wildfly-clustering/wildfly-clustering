/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.wildfly.clustering.marshalling.ByteBufferMarshalledValueFactoryTestCase;

/**
 * @author Paul Ferraro
 */
public class ProtoStreamByteBufferMarshalledValueFactoryTestCase extends ByteBufferMarshalledValueFactoryTestCase {

	public ProtoStreamByteBufferMarshalledValueFactoryTestCase() {
		super(new ProtoStreamTesterFactory().get());
	}
}
