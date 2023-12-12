/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.user.coarse;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.infinispan.embedded.user.UserSessionsKey;
import org.wildfly.clustering.session.infinispan.embedded.user.UserSessionsKeyFormatter;

/**
 * Unit test for {@link UserSessionsKey}.
 * @author Paul Ferraro
 */
public class CoarseSessionsKeyTestCase {

	@Test
	public void test() throws IOException {
		test(new ProtoStreamTesterFactory().createTester());
		test(new FormatterTester<>(new UserSessionsKeyFormatter()));
	}

	private static void test(Tester<UserSessionsKey> tester) throws IOException {
		tester.test(new UserSessionsKey("ABC123"));
	}
}
