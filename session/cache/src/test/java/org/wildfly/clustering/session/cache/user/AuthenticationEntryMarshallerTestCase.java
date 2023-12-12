/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.MarshallingTester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link AuthenticationEntryExternalizer}.
 * @author Paul Ferraro
 */
public class AuthenticationEntryMarshallerTestCase {

	@Test
	public void test() throws IOException {
		MarshallingTester<UserContextEntry<String, Object>> tester = new ProtoStreamTesterFactory(List.of(new UserSerializationContextInitializer())).createTester();
		tester.test(new UserContextEntry<>("username"), AuthenticationEntryMarshallerTestCase::assertEquals);
	}

	static void assertEquals(UserContextEntry<String, Object> entry1, UserContextEntry<String, Object> entry2) {
		Assertions.assertEquals(entry1.getContext(), entry2.getContext());
	}
}
