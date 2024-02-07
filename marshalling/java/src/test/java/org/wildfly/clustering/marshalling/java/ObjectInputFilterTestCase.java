/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.ByteBufferTestMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTester;
import org.wildfly.clustering.marshalling.Tester;

/**
 * @author Paul Ferraro
 */
public class ObjectInputFilterTestCase {

	@Test
	public void test() throws IOException {
		ByteBufferMarshaller marshaller = new JavaByteBufferMarshaller(this.getClass().getClassLoader(), ObjectInputFilter.Config.createFilter("java.lang.*;!*"));
		Tester<Object> tester = new MarshallingTester<>(new ByteBufferTestMarshaller<>(marshaller), List.of());
		tester.test(Integer.valueOf(1));
		Assertions.assertThrows(InvalidClassException.class, () -> tester.test(TimeUnit.SECONDS));
	}
}
