/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.TesterFactory;

/**
 * @author Paul Ferraro
 */
public class ObjectInputFilterTestCase {

	@Test
	public void test() throws IOException {
		TesterFactory factory = new JavaSerializationTesterFactory(this.getClass().getClassLoader(), ObjectInputFilter.Config.createFilter("java.lang.*;!*"));
		Tester<Object> tester = factory.createTester();
		tester.accept(Integer.valueOf(1));
		tester.reject(TimeUnit.SECONDS, InvalidClassException.class);
	}
}
