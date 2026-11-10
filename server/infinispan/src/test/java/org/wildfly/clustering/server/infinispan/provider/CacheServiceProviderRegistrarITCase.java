/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationEvent;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationListener;
import org.wildfly.clustering.server.provider.ServiceProviderRegistryListener;

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

		BlockingQueue<Map.Entry<String, ServiceProviderRegistrationEvent<CacheContainerGroupMember>>> events = new LinkedBlockingQueue<>();
		BlockingQueue<ServiceProviderRegistrationEvent<CacheContainerGroupMember>> fooEvents = new LinkedBlockingQueue<>();
		BlockingQueue<ServiceProviderRegistrationEvent<CacheContainerGroupMember>> barEvents = new LinkedBlockingQueue<>();

		try (CacheContainerServiceProviderRegistrarContext<String> provider1 = new CacheContainerServiceProviderRegistrarContext<>(CLUSTER_NAME, MEMBER_1)) {
			CacheContainerServiceProviderRegistrar<String> registrar1 = provider1.get();
			CacheContainerGroupMember member1 = registrar1.getGroup().getLocalMember();
			assertThat(registrar1.getServices()).isEmpty();
			assertThat(registrar1.getProviders(foo)).isEmpty();
			assertThat(registrar1.getProviders(bar)).isEmpty();

			try (Registration registration1 = registrar1.register(new CollectingServiceProviderRegistryListener<>(events))) {

				assertThat(events).isEmpty();

				try (ServiceProviderRegistration<String, CacheContainerGroupMember> fooRegistration1 = registrar1.register(foo, new CollectingServiceProviderRegistrationListener(fooEvents))) {

					assertThat(fooRegistration1.getService()).isSameAs(foo);
					assertThat(fooRegistration1.getProviders()).containsExactly(member1);

					assertThat(registrar1.getServices()).containsExactly(foo);
					assertThat(registrar1.getProviders(foo)).containsExactly(member1);

					Map.Entry<String, ServiceProviderRegistrationEvent<CacheContainerGroupMember>> event = events.poll(10L, TimeUnit.SECONDS);
					assertThat(event).isNotNull();
					assertThat(event.getKey()).isEqualTo(foo);
					assertThat(event.getValue().getPreviousProviders()).isEmpty();
					assertThat(event.getValue().getCurrentProviders()).containsExactly(member1);

					ServiceProviderRegistrationEvent<CacheContainerGroupMember> fooEvent = fooEvents.poll(10L, TimeUnit.SECONDS);
					assertThat(fooEvent).isNotNull();
					assertThat(fooEvent.getPreviousProviders()).isEmpty();
					assertThat(fooEvent.getCurrentProviders()).containsExactly(member1);

					try (ServiceProviderRegistration<String, CacheContainerGroupMember> barRegistration1 = registrar1.register(bar, new CollectingServiceProviderRegistrationListener(barEvents))) {
						assertThat(barRegistration1.getService()).isSameAs(bar);
						assertThat(barRegistration1.getProviders()).containsExactly(member1);

						assertThat(registrar1.getServices()).containsExactlyInAnyOrder(foo, bar);
						assertThat(registrar1.getProviders(bar)).containsExactly(member1);

						event = events.poll(10L, TimeUnit.SECONDS);
						assertThat(event).isNotNull();
						assertThat(event.getKey()).isEqualTo(bar);
						assertThat(event.getValue().getPreviousProviders()).isEmpty();
						assertThat(event.getValue().getCurrentProviders()).containsExactly(member1);
						assertThat(events).isEmpty();

						ServiceProviderRegistrationEvent<CacheContainerGroupMember> barEvent = barEvents.poll(10L, TimeUnit.SECONDS);
						assertThat(barEvent).isNotNull();
						assertThat(barEvent.getPreviousProviders()).isEmpty();
						assertThat(barEvent.getCurrentProviders()).containsExactly(member1);
						assertThat(barEvents).isEmpty();

						CacheContainerGroupMember member2 = null;
						try (CacheContainerServiceProviderRegistrarContext<String> provider2 = new CacheContainerServiceProviderRegistrarContext<>(CLUSTER_NAME, MEMBER_2)) {
							CacheContainerServiceProviderRegistrar<String> registrar2 = provider2.get();
							member2 = registrar2.getGroup().getLocalMember();

							assertThat(registrar2.getServices()).containsExactlyInAnyOrder(foo, bar);
							assertThat(registrar2.getProviders(foo)).containsExactly(member1);
							assertThat(registrar2.getProviders(bar)).containsExactly(member1);

							assertThat(events).isEmpty();
							assertThat(fooEvents).isEmpty();
							assertThat(barEvents).isEmpty();

							try (ServiceProviderRegistration<String, CacheContainerGroupMember> fooRegistration2 = registrar2.register(foo)) {
								assertThat(fooRegistration2.getService()).isSameAs(foo);
								assertThat(fooRegistration2.getProviders()).containsExactlyInAnyOrder(member1, member2);

								assertThat(registrar1.getProviders(foo)).containsExactlyInAnyOrder(member1, member2);
								assertThat(registrar2.getProviders(foo)).containsExactlyInAnyOrder(member1, member2);

								event = events.poll(10L, TimeUnit.SECONDS);
								assertThat(event).isNotNull();
								assertThat(event.getKey()).isEqualTo(foo);
								assertThat(event.getValue().getPreviousProviders()).containsExactly(member1);
								assertThat(event.getValue().getCurrentProviders()).containsExactlyInAnyOrder(member1, member2);
								assertThat(events).isEmpty();

								fooEvent = fooEvents.poll(10L, TimeUnit.SECONDS);
								assertThat(fooEvent).isNotNull();
								assertThat(fooEvent.getPreviousProviders()).containsExactly(member1);
								assertThat(fooEvent.getCurrentProviders()).containsExactlyInAnyOrder(member1, member2);
								assertThat(fooEvents).isEmpty();
							}

							event = events.poll(10L, TimeUnit.SECONDS);
							assertThat(event).isNotNull();
							assertThat(event.getKey()).isEqualTo(foo);
							assertThat(event.getValue().getPreviousProviders()).containsExactlyInAnyOrder(member1, member2);
							assertThat(event.getValue().getCurrentProviders()).containsExactly(member1);
							assertThat(events).isEmpty();

							fooEvent = fooEvents.poll(10L, TimeUnit.SECONDS);
							assertThat(fooEvent).isNotNull();
							assertThat(fooEvent.getPreviousProviders()).containsExactlyInAnyOrder(member1, member2);
							assertThat(fooEvent.getCurrentProviders()).containsExactly(member1);
							assertThat(fooEvents).isEmpty();

							// Validate closing registry without closing registration
							ServiceProviderRegistration<String, CacheContainerGroupMember> barRegistration2 = registrar2.register(bar);
							assertThat(barRegistration2.getService()).isSameAs(bar);
							assertThat(barRegistration2.getProviders()).containsExactlyInAnyOrder(member1, member2);

							assertThat(registrar1.getProviders(bar)).containsExactlyInAnyOrder(member1, member2);
							assertThat(registrar2.getProviders(bar)).containsExactlyInAnyOrder(member1, member2);

							event = events.poll(10L, TimeUnit.SECONDS);
							assertThat(event).isNotNull();
							assertThat(event.getKey()).isEqualTo(bar);
							assertThat(event.getValue().getPreviousProviders()).containsExactly(member1);
							assertThat(event.getValue().getCurrentProviders()).containsExactlyInAnyOrder(member1, member2);
							assertThat(events).isEmpty();

							barEvent = barEvents.poll(10L, TimeUnit.SECONDS);
							assertThat(barEvent).isNotNull();
							assertThat(barEvent.getPreviousProviders()).containsExactly(member1);
							assertThat(barEvent.getCurrentProviders()).containsExactlyInAnyOrder(member1, member2);
							assertThat(barEvents).isEmpty();
						}

						event = events.poll(10L, TimeUnit.SECONDS);
						assertThat(event).isNotNull();
						assertThat(event.getKey()).isEqualTo(bar);
						assertThat(event.getValue().getPreviousProviders()).containsExactlyInAnyOrder(member1, member2);
						assertThat(event.getValue().getCurrentProviders()).containsExactly(member1);
						assertThat(events.isEmpty());

						barEvent = barEvents.poll(10L, TimeUnit.SECONDS);
						assertThat(barEvent.getCurrentProviders()).containsExactly(member1);
						assertThat(barEvent.getPreviousProviders()).containsExactlyInAnyOrder(member1, member2);
						assertThat(barEvents).isEmpty();
						assertThat(fooEvents).isEmpty();

						assertThat(registrar1.getServices()).containsExactlyInAnyOrder(foo, bar);
						assertThat(registrar1.getProviders(foo)).containsExactly(member1);
						assertThat(registrar1.getProviders(bar)).containsExactly(member1);
					}

					assertThat(registrar1.getServices()).containsExactly(foo);
					assertThat(registrar1.getProviders(foo)).containsExactly(member1);
					assertThat(registrar1.getProviders(bar)).isEmpty();

					event = events.poll(10L, TimeUnit.SECONDS);
					assertThat(event).isNotNull();
					assertThat(event.getKey()).isEqualTo(bar);
					assertThat(event.getValue().getPreviousProviders()).containsExactly(member1);
					assertThat(event.getValue().getCurrentProviders()).isEmpty();
					assertThat(events.isEmpty());

					ServiceProviderRegistrationEvent<CacheContainerGroupMember> barEvent = barEvents.poll(10L, TimeUnit.SECONDS);
					assertThat(barEvent.getCurrentProviders()).isEmpty();
					assertThat(barEvent.getPreviousProviders()).containsExactly(member1);
					assertThat(barEvents).isEmpty();
					assertThat(fooEvents).isEmpty();
				}

				assertThat(registrar1.getServices()).isEmpty();
				assertThat(registrar1.getProviders(foo)).isEmpty();
				assertThat(registrar1.getProviders(bar)).isEmpty();

				Map.Entry<String, ServiceProviderRegistrationEvent<CacheContainerGroupMember>> event = events.poll(10L, TimeUnit.SECONDS);
				assertThat(event).isNotNull();
				assertThat(event.getKey()).isEqualTo(foo);
				assertThat(event.getValue().getPreviousProviders()).containsExactly(member1);
				assertThat(event.getValue().getCurrentProviders()).isEmpty();
				assertThat(events.isEmpty());

				ServiceProviderRegistrationEvent<CacheContainerGroupMember> fooEvent = fooEvents.poll(10L, TimeUnit.SECONDS);
				assertThat(fooEvent.getCurrentProviders()).isEmpty();
				assertThat(fooEvent.getPreviousProviders()).containsExactly(member1);
				assertThat(fooEvents).isEmpty();
				assertThat(barEvents).isEmpty();
			}
		}

		assertThat(events).isEmpty();
		assertThat(fooEvents).isEmpty();
		assertThat(barEvents).isEmpty();
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

	private static class CollectingServiceProviderRegistryListener<T> implements ServiceProviderRegistryListener<T, CacheContainerGroupMember> {
		private final Collection<Map.Entry<T, ServiceProviderRegistrationEvent<CacheContainerGroupMember>>> events;

		CollectingServiceProviderRegistryListener(Collection<Map.Entry<T, ServiceProviderRegistrationEvent<CacheContainerGroupMember>>> events) {
			this.events = events;
		}

		@Override
		public void providersChanged(T service, ServiceProviderRegistrationEvent<CacheContainerGroupMember> event) {
			this.events.add(Map.entry(service, event));
		}
	}
}
