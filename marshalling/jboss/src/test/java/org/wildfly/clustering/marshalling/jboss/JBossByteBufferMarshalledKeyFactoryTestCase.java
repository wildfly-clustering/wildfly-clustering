/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractByteBufferMarshalledKeyFactoryTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossByteBufferMarshalledKeyFactoryTestCase extends AbstractByteBufferMarshalledKeyFactoryTestCase {

	public JBossByteBufferMarshalledKeyFactoryTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
