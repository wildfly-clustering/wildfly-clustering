/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationEvent;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationListener;

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

		try (CacheContainerServiceProviderRegistrarContext<String> provider1 = new CacheContainerServiceProviderRegistrarContext<>(CLUSTER_NAME, MEMBER_1)) {
			CacheContainerServiceProviderRegistrar<String> registrar1 = provider1.get();
			CacheContainerGroupMember member1 = registrar1.getGroup().getLocalMember();
			assertThat(registrar1.getServices()).isEmpty();
			assertThat(registrar1.getProviders(foo)).isEmpty();
			assertThat(registrar1.getProviders(bar)).isEmpty();

			BlockingQueue<ServiceProviderRegistrationEvent<CacheContainerGroupMember>> fooEvents = new LinkedBlockingQueue<>();
			try (ServiceProviderRegistration<String, CacheContainerGroupMember> fooRegistration1 = registrar1.register(foo, new CollectingServiceProviderRegistrationListener(fooEvents))) {
				assertThat(fooRegistration1.getService()).isSameAs(foo);
				assertThat(fooRegistration1.getProviders()).containsExactly(member1);

				assertThat(registrar1.getServices()).containsExactly(foo);
				assertThat(registrar1.getProviders(foo)).containsExactly(member1);

				ServiceProviderRegistrationEvent<CacheContainerGroupMember> event = fooEvents.poll(5, TimeUnit.SECONDS);
				assertThat(event.getCurrentProviders()).containsExactly(member1);
				assertThat(event.getPreviousProviders()).isEmpty();

				BlockingQueue<ServiceProviderRegistrationEvent<CacheContainerGroupMember>> barEvents = new LinkedBlockingQueue<>();
				try (ServiceProviderRegistration<String, CacheContainerGroupMember> barRegistration1 = registrar1.register(bar, new CollectingServiceProviderRegistrationListener(barEvents))) {
					assertThat(barRegistration1.getService()).isSameAs(bar);
					assertThat(barRegistration1.getProviders()).containsExactly(member1);

					assertThat(registrar1.getServices()).containsExactlyInAnyOrder(foo, bar);
					assertThat(registrar1.getProviders(bar)).containsExactly(member1);

					event = barEvents.poll(5, TimeUnit.SECONDS);
					assertThat(event.getCurrentProviders()).containsExactly(member1);
					assertThat(event.getPreviousProviders()).isEmpty();

					CacheContainerGroupMember member2 = null;
					try (CacheContainerServiceProviderRegistrarContext<String> provider2 = new CacheContainerServiceProviderRegistrarContext<>(CLUSTER_NAME, MEMBER_2)) {
						CacheContainerServiceProviderRegistrar<String> registrar2 = provider2.get();
						member2 = registrar2.getGroup().getLocalMember();

						assertThat(registrar2.getServices()).containsExactlyInAnyOrder(foo, bar);
						assertThat(registrar2.getProviders(foo)).containsExactly(member1);
						assertThat(registrar2.getProviders(bar)).containsExactly(member1);

						try (ServiceProviderRegistration<String, CacheContainerGroupMember> fooRegistration2 = registrar2.register(foo)) {
							assertThat(fooRegistration2.getService()).isSameAs(foo);
							assertThat(fooRegistration2.getProviders()).containsExactlyInAnyOrder(member1, member2);

							assertThat(registrar1.getProviders(foo)).containsExactlyInAnyOrder(member1, member2);
							assertThat(registrar2.getProviders(foo)).containsExactlyInAnyOrder(member1, member2);

							event = fooEvents.poll(5, TimeUnit.SECONDS);
						}

						event = fooEvents.poll(5, TimeUnit.SECONDS);
						assertThat(event.getCurrentProviders()).containsExactly(member1);
						assertThat(event.getPreviousProviders()).containsExactlyInAnyOrder(member1, member2);

						// Validate closing registry without closing registration
						ServiceProviderRegistration<String, CacheContainerGroupMember> barRegistration2 = registrar2.register(bar);
						assertThat(barRegistration2.getService()).isSameAs(bar);
						assertThat(barRegistration2.getProviders()).containsExactlyInAnyOrder(member1, member2);

						assertThat(registrar1.getProviders(bar)).containsExactlyInAnyOrder(member1, member2);
						assertThat(registrar2.getProviders(bar)).containsExactlyInAnyOrder(member1, member2);

						event = barEvents.poll(5, TimeUnit.SECONDS);
						assertThat(event.getCurrentProviders()).containsExactlyInAnyOrder(member1, member2);
						assertThat(event.getPreviousProviders()).containsExactly(member1);
					}

					assertThat(fooEvents.poll(100, TimeUnit.MILLISECONDS)).isNull();

					assertThat(registrar1.getServices()).containsExactlyInAnyOrder(foo, bar);
					assertThat(registrar1.getProviders(foo)).containsExactly(member1);
					assertThat(registrar1.getProviders(bar)).containsExactly(member1);

					event = barEvents.poll(5, TimeUnit.SECONDS);
					assertThat(event.getCurrentProviders()).containsExactly(member1);
					assertThat(event.getPreviousProviders()).containsExactlyInAnyOrder(member1, member2);
				}

				assertThat(registrar1.getServices()).containsExactly(foo);
				assertThat(registrar1.getProviders(foo)).containsExactly(member1);
				assertThat(registrar1.getProviders(bar)).isEmpty();

				assertThat(barEvents.poll(100, TimeUnit.MILLISECONDS)).isNull();
			}

			assertThat(registrar1.getServices()).isEmpty();
			assertThat(registrar1.getProviders(foo)).isEmpty();
			assertThat(registrar1.getProviders(bar)).isEmpty();

			assertThat(fooEvents.poll(100, TimeUnit.MILLISECONDS)).isNull();
		}
	}

	private static class CollectingServiceProviderRegistrationListener implements ServiceProviderRegistrationListener<CacheContainerGroupMember> {
		private final Collection<ServiceProviderRegistrationEvent<CacheContainerGroupMember>> events;

		CollectingServiceProviderRegistrationListener(Collection<ServiceProviderRegistrationEvent<CacheContainerGroupMember>> events) {
			this.events = events;
		}

		@Override
		public void providersChanged(ServiceProviderRegistrationEvent<CacheContainerGroupMember> event) {
			this.events.add(event);
		}
	}
}
