/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractCircularReferenceTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossCircularReferenceTestCase extends AbstractCircularReferenceTestCase {

	public JBossCircularReferenceTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
