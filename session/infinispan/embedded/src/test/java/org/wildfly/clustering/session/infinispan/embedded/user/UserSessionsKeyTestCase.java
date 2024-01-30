/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link UserSessionsKey}.
 * @author Paul Ferraro
 */
public class UserSessionsKeyTestCase {

	@Test
	public void test() throws IOException {
		test(ProtoStreamTesterFactory.INSTANCE.createTester());
		test(new FormatterTester<>(new UserSessionsKeyFormatter()));
	}

	private static void test(Tester<UserSessionsKey> tester) throws IOException {
		tester.test(new UserSessionsKey("ABC123"));
	}
}
