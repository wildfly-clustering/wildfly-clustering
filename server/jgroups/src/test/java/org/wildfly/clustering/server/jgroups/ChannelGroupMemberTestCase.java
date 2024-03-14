/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.net.InetAddress;
import java.util.function.Consumer;

import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.TwoWayKey2StringMapperTesterFactory;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * @author Paul Ferraro
 */
public class ChannelGroupMemberTestCase {

	@ParameterizedTest
	@TesterFactorySource({ MarshallingTesterFactory.class, TwoWayKey2StringMapperTesterFactory.class })
	public void test(TesterFactory factory) {
		Consumer<ChannelGroupMember> tester = factory.createTester();
		tester.accept(new JChannelGroupMember(UUID.randomUUID()));
		tester.accept(new JChannelGroupMember(new IpAddress(InetAddress.getLoopbackAddress(), Short.MAX_VALUE)));
	}
}
