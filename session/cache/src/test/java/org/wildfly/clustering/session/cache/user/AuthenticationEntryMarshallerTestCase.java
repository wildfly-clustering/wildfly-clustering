/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link UserContextEntry} marshalling.
 * @author Paul Ferraro
 */
public class AuthenticationEntryMarshallerTestCase {

	@Test
	public void test() {
		Consumer<UserContextEntry<String, Object>> tester = new ProtoStreamTesterFactory(new UserSerializationContextInitializer()).createTester(AuthenticationEntryMarshallerTestCase::assertEquals);
		tester.accept(new UserContextEntry<>("username"));
	}

	static void assertEquals(UserContextEntry<String, Object> entry1, UserContextEntry<String, Object> entry2) {
		assertThat(entry2.getPersistentContext()).isEqualTo(entry1.getPersistentContext());
	}
}
