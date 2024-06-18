/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.FormatterTesterFactory;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.server.jgroups.JChannelGroupMember;
import org.wildfly.clustering.server.local.LocalGroupMember;

/**
 * @author Paul Ferraro
 */
public class LocalEmbeddedCacheManagerGroupMemberTestCase {

	@ParameterizedTest
	@TesterFactorySource({ MarshallingTesterFactory.class, FormatterTesterFactory.class })
	public void test(TesterFactory factory) {
		factory.createTester().accept(new LocalEmbeddedCacheManagerGroupMember("foo"));
	}

	@Test
	public void test() {
		CacheContainerGroupMember member = new LocalEmbeddedCacheManagerGroupMember("foo");
		Assertions.assertEquals("foo", member.getName());
		Assertions.assertEquals(member, new LocalEmbeddedCacheManagerGroupMember("foo"));
		Assertions.assertEquals(member.hashCode(), new LocalEmbeddedCacheManagerGroupMember("foo").hashCode());
		Assertions.assertEquals(member, LocalGroupMember.of("foo"));
		Assertions.assertEquals(member.hashCode(), LocalGroupMember.of("foo").hashCode());
		Assertions.assertNotEquals(member, new EmbeddedCacheManagerGroupMember(new JGroupsAddress(UUID.randomUUID())));
		Assertions.assertNotEquals(member, new JChannelGroupMember(UUID.randomUUID()));
		Assertions.assertNotEquals(member, new LocalEmbeddedCacheManagerGroupMember("bar"));
		Assertions.assertNotEquals(member, LocalGroupMember.of("bar"));
	}
}
