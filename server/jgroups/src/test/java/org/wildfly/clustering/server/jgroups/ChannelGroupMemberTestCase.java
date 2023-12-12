/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.io.IOException;
import java.net.InetAddress;

import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.java.JavaTesterFactory;
import org.wildfly.clustering.marshalling.jboss.JBossTesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class ChannelGroupMemberTestCase {

	@Test
	public void test() throws IOException {
		test(JavaTesterFactory.INSTANCE.createTester());
		test(JBossTesterFactory.INSTANCE.createTester());
		test(new ProtoStreamTesterFactory().createTester());
	}

	private static void test(Tester<ChannelGroupMember> tester) throws IOException {
		tester.test(new JChannelGroupMember(UUID.randomUUID()));
		tester.test(new JChannelGroupMember(new IpAddress(InetAddress.getLoopbackAddress(), Short.MAX_VALUE)));
	}
}
