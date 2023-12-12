/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.util.UUID;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.KeyMapperTester;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.server.infinispan.JGroupsAddressSerializer.JGroupsAddressFormatter;

/**
 * @author Paul Ferraro
 */
public class JGroupsAddressTestCase {

	private final JGroupsAddress address = new JGroupsAddress(UUID.randomUUID());

	@Test
	public void test() throws IOException {
		test(new FormatterTester<>(new JGroupsAddressFormatter()));
		test(new KeyMapperTester<>(new KeyMapper()));
		test(new ProtoStreamTesterFactory().createTester());
	}

	private void test(Tester<JGroupsAddress> tester) throws IOException {
		tester.test(this.address);
	}
}
