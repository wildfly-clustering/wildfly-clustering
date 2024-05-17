/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderListener;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;

/**
 * @author Paul Ferraro
 */
public class CacheServiceProviderRegistrarITCase {
	private static final String CLUSTER_NAME = "cluster";
	private static final String MEMBER_1 = "member1";
	private static final String MEMBER_2 = "member2";

	@Test
	public void test() throws Exception {
		String foo = "foo";
		String bar = "bar";

		try (CacheContainerServiceProviderRegistrarProvider<String> provider1 = new CacheContainerServiceProviderRegistrarProvider<>(CLUSTER_NAME, MEMBER_1)) {
			CacheContainerServiceProviderRegistrar<String> registrar1 = provider1.get();
			CacheContainerGroupMember member1 = registrar1.getGroup().getLocalMember();
			assertEquals(Set.of(), registrar1.getServices());
			assertEquals(Set.of(), registrar1.getProviders(foo));
			assertEquals(Set.of(), registrar1.getProviders(bar));

			BlockingQueue<Set<CacheContainerGroupMember>> fooEvents = new LinkedBlockingQueue<>();
			try (ServiceProviderRegistration<String, CacheContainerGroupMember> fooRegistration1 = registrar1.register(foo, new CollectionServiceProviderListener(fooEvents))) {
				assertSame(foo, fooRegistration1.getService());
				assertEquals(Set.of(member1), fooRegistration1.getProviders());

				assertEquals(Set.of(foo), registrar1.getServices());
				assertEquals(Set.of(member1), registrar1.getProviders(foo));

				assertEquals(Set.of(member1), fooEvents.poll(5, TimeUnit.SECONDS));

				BlockingQueue<Set<CacheContainerGroupMember>> barEvents = new LinkedBlockingQueue<>();
				try (ServiceProviderRegistration<String, CacheContainerGroupMember> barRegistration1 = registrar1.register(bar, new CollectionServiceProviderListener(barEvents))) {
					assertSame(bar, barRegistration1.getService());
					assertEquals(Set.of(member1), barRegistration1.getProviders());

					assertEquals(Set.of(foo, bar), registrar1.getServices());
					assertEquals(Set.of(member1), registrar1.getProviders(bar));

					assertEquals(Set.of(member1), barEvents.poll(5, TimeUnit.SECONDS));

					try (CacheContainerServiceProviderRegistrarProvider<String> provider2 = new CacheContainerServiceProviderRegistrarProvider<>(CLUSTER_NAME, MEMBER_2)) {
						CacheContainerServiceProviderRegistrar<String> registrar2 = provider2.get();
						CacheContainerGroupMember member2 = registrar2.getGroup().getLocalMember();

						assertEquals(Set.of(foo, bar), registrar2.getServices());
						assertEquals(Set.of(member1), registrar2.getProviders(foo));
						assertEquals(Set.of(member1), registrar2.getProviders(bar));

						try (ServiceProviderRegistration<String, CacheContainerGroupMember> fooRegistration2 = registrar2.register(foo)) {
							assertSame(foo, fooRegistration2.getService());
							assertEquals(Set.of(member1, member2), fooRegistration2.getProviders());

							assertEquals(Set.of(member1, member2), registrar1.getProviders(foo));
							assertEquals(Set.of(member1, member2), registrar2.getProviders(foo));

							assertEquals(Set.of(member1, member2), fooEvents.poll(5, TimeUnit.SECONDS));
						}

						assertEquals(Set.of(member1), fooEvents.poll(5, TimeUnit.SECONDS));

						// Validate closing registry without closing registration
						ServiceProviderRegistration<String, CacheContainerGroupMember> barRegistration2 = registrar2.register(bar);
						assertSame(bar, barRegistration2.getService());
						assertEquals(Set.of(member1, member2), barRegistration2.getProviders());

						assertEquals(Set.of(member1, member2), registrar1.getProviders(bar));
						assertEquals(Set.of(member1, member2), registrar2.getProviders(bar));

						assertEquals(Set.of(member1, member2), barEvents.poll(5, TimeUnit.SECONDS));
					}

					assertNull(fooEvents.poll(100, TimeUnit.MILLISECONDS));

					assertEquals(Set.of(foo, bar), registrar1.getServices());
					assertEquals(Set.of(member1), registrar1.getProviders(foo));
					assertEquals(Set.of(member1), registrar1.getProviders(bar));

					assertEquals(Set.of(member1), barEvents.poll(5, TimeUnit.SECONDS));
				}

				assertEquals(Set.of(foo), registrar1.getServices());
				assertEquals(Set.of(member1), registrar1.getProviders(foo));
				assertEquals(Set.of(), registrar1.getProviders(bar));

				assertNull(barEvents.poll(100, TimeUnit.MILLISECONDS));
			}

			assertEquals(Set.of(), registrar1.getServices());
			assertEquals(Set.of(), registrar1.getProviders(foo));
			assertEquals(Set.of(), registrar1.getProviders(bar));

			assertNull(fooEvents.poll(100, TimeUnit.MILLISECONDS));
		}
	}

	private static class CollectionServiceProviderListener implements ServiceProviderListener<CacheContainerGroupMember> {
		private final Collection<Set<CacheContainerGroupMember>> events;

		CollectionServiceProviderListener(Collection<Set<CacheContainerGroupMember>> events) {
			this.events = events;
		}

		@Override
		public void providersChanged(Set<CacheContainerGroupMember> providers) {
			this.events.add(providers);
		}
	}
}
