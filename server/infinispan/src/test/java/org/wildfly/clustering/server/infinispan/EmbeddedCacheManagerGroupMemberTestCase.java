/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.util.UUID;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.jboss.JBossTesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit tests for {@link AddressableNodeSerializer}.
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMemberTestCase {

	private final EmbeddedCacheManagerGroupMember member = new EmbeddedCacheManagerGroupMember(new JGroupsAddress(UUID.randomUUID()));

	@Test
	public void test() throws IOException {
		this.test(new FormatterTester<>(new EmbeddedCacheManagerGroupMemberSerializer.AddressGroupMemberFormatter()));
		this.test(JBossTesterFactory.INSTANCE.createTester());
		this.test(new ProtoStreamTesterFactory().createTester());
	}

	public void test(Tester<EmbeddedCacheManagerGroupMember> tester) throws IOException {
		tester.test(this.member);
	}
}
