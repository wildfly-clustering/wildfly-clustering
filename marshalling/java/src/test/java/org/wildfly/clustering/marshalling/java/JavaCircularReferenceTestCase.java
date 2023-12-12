/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractCircularReferenceTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaCircularReferenceTestCase extends AbstractCircularReferenceTestCase {

	public JavaCircularReferenceTestCase() {
		super(JavaTesterFactory.INSTANCE);
	}
}
