/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractAtomicTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossAtomicTestCase extends AbstractAtomicTestCase {

	public JBossAtomicTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
