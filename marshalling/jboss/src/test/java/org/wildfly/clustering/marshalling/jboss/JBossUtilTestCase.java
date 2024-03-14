/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractUtilTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossUtilTestCase extends AbstractUtilTestCase {

	public JBossUtilTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
