/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.wildfly.clustering.marshalling.AbstractByteBufferMarshalledKeyFactoryTestCase;

/**
 * @author Paul Ferraro
 */
public class ProtoStreamByteBufferMarshalledKeyFactoryTestCase extends AbstractByteBufferMarshalledKeyFactoryTestCase {

	public ProtoStreamByteBufferMarshalledKeyFactoryTestCase() {
		super(new ProtoStreamTesterFactory());
	}
}
