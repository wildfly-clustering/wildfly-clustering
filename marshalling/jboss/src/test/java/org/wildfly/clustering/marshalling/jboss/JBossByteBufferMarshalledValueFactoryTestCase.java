/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractByteBufferMarshalledValueFactoryTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossByteBufferMarshalledValueFactoryTestCase extends AbstractByteBufferMarshalledValueFactoryTestCase {

	public JBossByteBufferMarshalledValueFactoryTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
