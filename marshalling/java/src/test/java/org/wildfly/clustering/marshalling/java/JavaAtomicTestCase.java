/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractAtomicTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaAtomicTestCase extends AbstractAtomicTestCase {

	public JavaAtomicTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
