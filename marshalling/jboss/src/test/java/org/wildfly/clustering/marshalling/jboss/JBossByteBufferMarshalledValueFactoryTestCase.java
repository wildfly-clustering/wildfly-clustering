/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.ByteBufferMarshalledValueFactoryTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossByteBufferMarshalledValueFactoryTestCase extends ByteBufferMarshalledValueFactoryTestCase {

	public JBossByteBufferMarshalledValueFactoryTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
