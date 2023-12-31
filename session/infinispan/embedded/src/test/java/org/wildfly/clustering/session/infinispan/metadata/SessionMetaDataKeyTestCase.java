/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.metadata;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKeyFormatter;

/**
 * Unit test for {@link SessionCreationMetaDataKey}.
 * @author Paul Ferraro
 */
public class SessionMetaDataKeyTestCase {

	@Test
	public void test() throws IOException {
		test(ProtoStreamTesterFactory.INSTANCE.createTester());
		test(new FormatterTester<>(new SessionMetaDataKeyFormatter()));
	}

	private static void test(Tester<SessionMetaDataKey> tester) throws IOException {
		tester.test(new SessionMetaDataKey("ABC123"));
	}
}
