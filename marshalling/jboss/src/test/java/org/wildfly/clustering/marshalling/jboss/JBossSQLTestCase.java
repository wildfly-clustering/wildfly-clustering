/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.wildfly.clustering.marshalling.AbstractSQLTestCase;

/**
 * @author Paul Ferraro
 */
public class JBossSQLTestCase extends AbstractSQLTestCase {

	public JBossSQLTestCase() {
		super(new JBossMarshallingTesterFactory());
	}
}
