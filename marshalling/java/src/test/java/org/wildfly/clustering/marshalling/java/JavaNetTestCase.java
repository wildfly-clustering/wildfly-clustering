/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractNetTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaNetTestCase extends AbstractNetTestCase {

	public JavaNetTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
