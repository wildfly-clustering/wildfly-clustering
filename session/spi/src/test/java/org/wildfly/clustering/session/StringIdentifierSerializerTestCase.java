/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Supplier;

import io.undertow.server.session.SecureRandomSessionIdGenerator;

import org.apache.catalina.util.StandardSessionIdGenerator;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Marshaller;

/**
 * Unit test for {@link IdentifierMarshaller}.
 * @author Paul Ferraro
 */
public class StringIdentifierSerializerTestCase {

	@Test
	public void testString() throws IOException {
		test(IdentifierMarshaller.ISO_LATIN_1, () -> UUID.randomUUID().toString());
	}

	@Test
	public void testBase64() throws IOException {
		io.undertow.server.session.SessionIdGenerator generator = new SecureRandomSessionIdGenerator();
		test(IdentifierMarshaller.BASE64, generator::createSessionId);
	}

	@Test
	public void testHex() throws IOException {
		org.apache.catalina.SessionIdGenerator generator = new StandardSessionIdGenerator();
		test(IdentifierMarshaller.HEX_UPPER, generator::generateSessionId);
	}

	private static void test(Marshaller<String, ByteBuffer> marshaller, Supplier<String> generator) throws IOException {
		for (int i = 0; i < 100; ++i) {
			String id = generator.get();
			ByteBuffer buffer = marshaller.write(id);
			assertThat(marshaller.read(buffer)).isEqualTo(id);
		}
	}
}
