/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import static org.assertj.core.api.Assertions.*;

import org.infinispan.remoting.transport.Address;
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
		String name = "foo";
		CacheContainerGroupMember member = new LocalEmbeddedCacheManagerGroupMember(name);

		assertThat(member.getName()).isSameAs(name);
		assertThat(member).hasSameHashCodeAs(name)
				.isEqualTo(new LocalEmbeddedCacheManagerGroupMember(name))
				.isEqualTo(LocalGroupMember.of(name))
				.isNotEqualTo(new LocalEmbeddedCacheManagerGroupMember("bar"))
				.isNotEqualTo(LocalGroupMember.of("bar"))
				.isNotEqualTo(new EmbeddedCacheManagerGroupMember(Address.random()))
				.isNotEqualTo(new JChannelGroupMember(UUID.randomUUID()))
				;
	}
}
