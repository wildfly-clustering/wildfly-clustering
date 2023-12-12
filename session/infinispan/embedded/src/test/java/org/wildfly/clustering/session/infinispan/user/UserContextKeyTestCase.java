/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.user;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.infinispan.embedded.user.UserContextKey;
import org.wildfly.clustering.session.infinispan.embedded.user.UserContextKeyFormatter;

/**
 * Unit test for {@link UserContextKey}.
 * @author Paul Ferraro
 */
public class UserContextKeyTestCase {

	@Test
	public void test() throws IOException {
		test(new ProtoStreamTesterFactory().createTester());
		test(new FormatterTester<>(new UserContextKeyFormatter()));
	}

	private static void test(Tester<UserContextKey> tester) throws IOException {
		tester.test(new UserContextKey("ABC123"));
	}
}
