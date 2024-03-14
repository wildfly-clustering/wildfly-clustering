/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractLangTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossLangTestCase extends AbstractLangTestCase {

	public JBossLangTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
