/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.ByteBufferMarshalledKeyFactoryTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossByteBufferMarshalledKeyFactoryTestCase extends ByteBufferMarshalledKeyFactoryTestCase {

	public JBossByteBufferMarshalledKeyFactoryTestCase() {
		super(JBossTesterFactory.INSTANCE.getMarshaller());
	}
}
