/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.Address;
import org.jgroups.util.NameCache;
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
 * Unit tests for {@link EmbeddedCacheManagerGroupMemberSerializer}.
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMemberTestCase {

	@ParameterizedTest
	@TesterFactorySource({ MarshallingTesterFactory.class, FormatterTesterFactory.class })
	public void test(TesterFactory factory) {
		factory.createTester().accept(new EmbeddedCacheManagerGroupMember(new JGroupsAddress(UUID.randomUUID())));
	}

	@Test
	public void test() {
		Address fooAddress = UUID.randomUUID();
		Address barAddress = UUID.randomUUID();
		NameCache.add(fooAddress, "foo");
		NameCache.add(barAddress, "bar");
		CacheContainerGroupMember fooMember = new EmbeddedCacheManagerGroupMember(new JGroupsAddress(fooAddress));
		Assertions.assertEquals("foo", fooMember.getName());
		CacheContainerGroupMember barMember = new EmbeddedCacheManagerGroupMember(new JGroupsAddress(barAddress));
		Assertions.assertEquals("bar", barMember.getName());

		Assertions.assertEquals(fooMember, new EmbeddedCacheManagerGroupMember(new JGroupsAddress(fooAddress)));
		Assertions.assertEquals(fooMember.hashCode(), new EmbeddedCacheManagerGroupMember(new JGroupsAddress(fooAddress)).hashCode());
		Assertions.assertNotEquals(fooMember, barMember);
		Assertions.assertEquals(fooMember, new JChannelGroupMember(fooAddress));
		Assertions.assertEquals(fooMember.hashCode(), new JChannelGroupMember(fooAddress).hashCode());
		Assertions.assertNotEquals(fooMember, new JChannelGroupMember(barAddress));
		Assertions.assertNotEquals(fooMember, new LocalEmbeddedCacheManagerGroupMember("foo"));
		Assertions.assertNotEquals(barMember, new LocalEmbeddedCacheManagerGroupMember("bar"));
		Assertions.assertNotEquals(fooMember, LocalGroupMember.of("foo"));
		Assertions.assertNotEquals(barMember, LocalGroupMember.of("bar"));
	}
}
