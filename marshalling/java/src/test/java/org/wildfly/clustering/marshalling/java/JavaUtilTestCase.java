/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractUtilTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaUtilTestCase extends AbstractUtilTestCase {

	public JavaUtilTestCase() {
		super(JavaTesterFactory.INSTANCE);
	}
}
