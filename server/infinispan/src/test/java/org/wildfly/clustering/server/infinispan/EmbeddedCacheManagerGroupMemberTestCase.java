/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.FormatterTesterFactory;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.server.local.LocalGroupMember;

/**
 * Unit tests for {@link EmbeddedCacheManagerGroupMemberSerializer}.
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMemberTestCase {

	@ParameterizedTest
	@TesterFactorySource({ MarshallingTesterFactory.class, FormatterTesterFactory.class })
	public void test(TesterFactory factory) {
		factory.createTester().accept(new EmbeddedCacheManagerGroupMember(Address.random()));
	}

	@Test
	public void test() {
		List<String> names = List.of("foo", "bar");
		List<Address> addresses = names.stream().map(Address::random).toList();
		List<CacheContainerGroupMember> members = addresses.stream().<CacheContainerGroupMember>map(EmbeddedCacheManagerGroupMember::new).toList();

		assertThat(members.get(0).getName()).isSameAs(names.get(0));
		assertThat(members.get(1).getName()).isSameAs(names.get(1));

		assertThat(members.get(0).getId()).isSameAs(addresses.get(0));
		assertThat(members.get(1).getId()).isSameAs(addresses.get(1));

		assertThat(members.get(0))
				.hasSameHashCodeAs(addresses.get(0))
				//.isEqualTo(new JChannelGroupMember(Address.toExtendedUUID(addresses.get(0))))
				.isNotEqualTo(new EmbeddedCacheManagerGroupMember(addresses.get(1)))
				//.isNotEqualTo(new JChannelGroupMember(Address.toExtendedUUID(addresses.get(1))))
				.isNotEqualTo(new LocalEmbeddedCacheManagerGroupMember("foo"))
				.isNotEqualTo(LocalGroupMember.of("foo"))
				;
		assertThat(members.get(1))
				.hasSameHashCodeAs(addresses.get(1))
				//.isEqualTo(new JChannelGroupMember(Address.toExtendedUUID(addresses.get(1))))
				.isNotEqualTo(new EmbeddedCacheManagerGroupMember(addresses.get(0)))
				//.isNotEqualTo(new JChannelGroupMember(Address.toExtendedUUID(addresses.get(0))))
				.isNotEqualTo(new LocalEmbeddedCacheManagerGroupMember("bar"))
				.isNotEqualTo(LocalGroupMember.of("bar"))
				;
	}
}
