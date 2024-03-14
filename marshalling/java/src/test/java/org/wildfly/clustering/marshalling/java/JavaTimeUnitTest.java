/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractTimeTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaTimeUnitTest extends AbstractTimeTestCase {

	public JavaTimeUnitTest() {
		super(new JavaSerializationTesterFactory());
	}
}
