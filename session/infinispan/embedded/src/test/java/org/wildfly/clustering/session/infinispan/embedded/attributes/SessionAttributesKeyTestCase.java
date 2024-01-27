/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded.attributes;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link SessionAttributesKey}.
 * @author Paul Ferraro
 */
public class SessionAttributesKeyTestCase {

	@Test
	public void test() throws IOException {
		test(ProtoStreamTesterFactory.INSTANCE.createTester());
		test(new FormatterTester<>(new SessionAttributesKeyFormatter()));
	}

	private static void test(Tester<SessionAttributesKey> tester) throws IOException {
		tester.test(new SessionAttributesKey("ABC123"));
	}
}
