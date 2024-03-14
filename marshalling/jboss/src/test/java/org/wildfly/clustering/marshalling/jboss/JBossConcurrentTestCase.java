/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractConcurrentTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossConcurrentTestCase extends AbstractConcurrentTestCase {

	public JBossConcurrentTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
