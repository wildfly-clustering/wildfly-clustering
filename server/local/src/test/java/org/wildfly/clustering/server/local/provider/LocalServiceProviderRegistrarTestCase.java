/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.provider;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationEvent;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationListener;
import org.wildfly.clustering.server.provider.ServiceProviderRegistryListener;

/**
 * Unit test for {@link LocalServiceProviderRegistrar}.
 * @author Paul Ferraro
 */
public class LocalServiceProviderRegistrarTestCase {

	@Test
	public void test() throws InterruptedException {
		LocalGroup group = mock(LocalGroup.class);
		LocalGroupMember localMember = mock(LocalGroupMember.class);
		BlockingQueue<Map.Entry<String, ServiceProviderRegistrationEvent<LocalGroupMember>>> events = new LinkedBlockingDeque<>();
		BlockingQueue<ServiceProviderRegistrationEvent<LocalGroupMember>> fooEvents = new LinkedBlockingDeque<>();

		ServiceProviderRegistryListener<String, LocalGroupMember> listener = new CollectingServiceProviderRegistryListener<>(events);
		ServiceProviderRegistrationListener<LocalGroupMember> fooListener = new CollectingServiceProviderRegistrationListener(fooEvents);

		doReturn(localMember).when(group).getLocalMember();

		try (LocalServiceProviderRegistrar<String> registrar = LocalServiceProviderRegistrar.of(group)) {
			assertThat(registrar.getGroup()).isSameAs(group);

			assertThat(registrar.getServices()).isEmpty();
			assertThat(registrar.getProviders("foo")).isEmpty();
			assertThat(registrar.getProviders("bar")).isEmpty();

			try (Registration registration = registrar.register(listener)) {
				assertThat(events).isEmpty();

				try (ServiceProviderRegistration<String, LocalGroupMember> foo = registrar.register("foo", fooListener)) {
					assertThat(foo.getService()).isEqualTo("foo");
					assertThat(foo.getProviders()).containsExactly(localMember);

					assertThat(registrar.getServices()).containsExactly("foo");
					assertThat(registrar.getProviders("foo")).containsExactly(localMember);
					assertThat(registrar.getProviders("bar")).isEmpty();

					Map.Entry<String, ServiceProviderRegistrationEvent<LocalGroupMember>> event = events.poll(10L, TimeUnit.SECONDS);
					assertThat(event).isNotNull();
					assertThat(event.getKey()).isEqualTo("foo");
					assertThat(event.getValue().getPreviousProviders()).isEmpty();
					assertThat(event.getValue().getCurrentProviders()).containsExactly(localMember);
					assertThat(events.isEmpty());

					ServiceProviderRegistrationEvent<LocalGroupMember> fooEvent = fooEvents.poll(10L, TimeUnit.SECONDS);
					assertThat(fooEvent).isNotNull();
					assertThat(fooEvent.getPreviousProviders()).isEmpty();
					assertThat(fooEvent.getCurrentProviders()).containsExactly(localMember);
					assertThat(fooEvents).isEmpty();

					try (ServiceProviderRegistration<String, LocalGroupMember> bar = registrar.register("bar")) {
						assertThat(bar.getService()).isEqualTo("bar");
						assertThat(bar.getProviders()).containsExactly(localMember);

						assertThat(registrar.getServices()).containsExactlyInAnyOrder("foo", "bar");
						assertThat(registrar.getProviders("foo")).containsExactly(localMember);
						assertThat(registrar.getProviders("bar")).containsExactly(localMember);

						event = events.poll(10L, TimeUnit.SECONDS);
						assertThat(event).isNotNull();
						assertThat(event.getKey()).isEqualTo("bar");
						assertThat(event.getValue().getPreviousProviders()).isEmpty();
						assertThat(event.getValue().getCurrentProviders()).containsExactly(localMember);
						assertThat(events).isEmpty();
						assertThat(fooEvents).isEmpty();
					}

					assertThat(registrar.getServices()).containsExactly("foo");
					assertThat(registrar.getProviders("foo")).containsExactly(localMember);
					assertThat(registrar.getProviders("bar")).isEmpty();

					event = events.poll(10L, TimeUnit.SECONDS);
					assertThat(event).isNotNull();
					assertThat(event.getKey()).isEqualTo("bar");
					assertThat(event.getValue().getPreviousProviders()).containsExactly(localMember);
					assertThat(event.getValue().getCurrentProviders()).isEmpty();
					assertThat(events).isEmpty();
					assertThat(fooEvents).isEmpty();
				}

				Map.Entry<String, ServiceProviderRegistrationEvent<LocalGroupMember>> eventEntry = events.poll(10L, TimeUnit.SECONDS);
				assertThat(eventEntry).isNotNull();
				assertThat(eventEntry.getKey()).isEqualTo("foo");
				ServiceProviderRegistrationEvent<LocalGroupMember> event = eventEntry.getValue();
				assertThat(event.getPreviousProviders()).containsExactly(localMember);
				assertThat(event.getCurrentProviders()).isEmpty();
				assertThat(events).isEmpty();

				ServiceProviderRegistrationEvent<LocalGroupMember> fooEvent = fooEvents.poll(10L, TimeUnit.SECONDS);
				assertThat(fooEvent).isNotNull();
				assertThat(fooEvent.getPreviousProviders()).containsExactly(localMember);
				assertThat(fooEvent.getCurrentProviders()).isEmpty();
				assertThat(fooEvents).isEmpty();

				assertThat(registrar.getServices()).isEmpty();
				assertThat(registrar.getProviders("foo")).isEmpty();
				assertThat(registrar.getProviders("bar")).isEmpty();
			}
		}
	}

	private static class CollectingServiceProviderRegistrationListener implements ServiceProviderRegistrationListener<LocalGroupMember> {
		private final Collection<ServiceProviderRegistrationEvent<LocalGroupMember>> events;

		CollectingServiceProviderRegistrationListener(Collection<ServiceProviderRegistrationEvent<LocalGroupMember>> events) {
			this.events = events;
		}

		@Override
		public void providersChanged(ServiceProviderRegistrationEvent<LocalGroupMember> event) {
			this.events.add(event);
		}
	}

	private static class CollectingServiceProviderRegistryListener<T> implements ServiceProviderRegistryListener<T, LocalGroupMember> {
		private final Collection<Map.Entry<T, ServiceProviderRegistrationEvent<LocalGroupMember>>> events;

		CollectingServiceProviderRegistryListener(Collection<Map.Entry<T, ServiceProviderRegistrationEvent<LocalGroupMember>>> events) {
			this.events = events;
		}

		@Override
		public void providersChanged(T service, ServiceProviderRegistrationEvent<LocalGroupMember> event) {
			this.events.add(Map.entry(service, event));
		}
	}
}
