/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractSQLTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaSQLTestCase extends AbstractSQLTestCase {

	public JavaSQLTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
