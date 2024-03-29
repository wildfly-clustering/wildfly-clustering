/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractNetTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossNetTestCase extends AbstractNetTestCase {

	public JBossNetTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
