/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import org.wildfly.clustering.marshalling.AbstractLangTestCase;

/**
 * @author Paul Ferraro
 */
public class JavaLangTestCase extends AbstractLangTestCase {

	public JavaLangTestCase() {
		super(new JavaSerializationTesterFactory());
	}
}
