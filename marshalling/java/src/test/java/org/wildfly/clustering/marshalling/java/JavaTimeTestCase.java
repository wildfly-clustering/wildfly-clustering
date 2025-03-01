/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractTimeTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaTimeTestCase extends AbstractTimeTestCase {

	public JavaTimeTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
