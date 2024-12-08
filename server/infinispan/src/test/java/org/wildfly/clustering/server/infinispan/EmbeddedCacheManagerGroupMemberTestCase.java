/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import static org.assertj.core.api.Assertions.*;

import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.Address;
import org.jgroups.util.NameCache;
import org.jgroups.util.UUID;
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
		assertThat(fooMember.getName()).isEqualTo("foo");
		CacheContainerGroupMember barMember = new EmbeddedCacheManagerGroupMember(new JGroupsAddress(barAddress));
		assertThat(barMember.getName()).isEqualTo("bar");

		assertThat(fooMember).hasSameHashCodeAs(fooAddress)
				.isEqualTo(new JChannelGroupMember(fooAddress))
				.isNotEqualTo(new JChannelGroupMember(barAddress))
				.isNotEqualTo(new LocalEmbeddedCacheManagerGroupMember("foo"))
				.isNotEqualTo(LocalGroupMember.of("foo"))
				;
		assertThat(barMember).hasSameHashCodeAs(barAddress)
				.isEqualTo(new JChannelGroupMember(barAddress))
				.isNotEqualTo(new JChannelGroupMember(fooAddress))
				.isNotEqualTo(new LocalEmbeddedCacheManagerGroupMember("bar"))
				.isNotEqualTo(LocalGroupMember.of("bar"))
				;
	}
}
