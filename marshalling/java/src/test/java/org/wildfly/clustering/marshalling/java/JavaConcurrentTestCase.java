/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractConcurrentTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaConcurrentTestCase extends AbstractConcurrentTestCase {

	public JavaConcurrentTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
