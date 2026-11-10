/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

import java.util.Set;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;

/**
 * A distributed registry of service providers.
 * Allows a client to query the group members that can provide a given service.
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProviderRegistrar<T, M extends GroupMember> extends ServiceProviderRegistry<T, M>, Registrar<T> {

	/**
	 * Registers the local group member as a provider of the specified service.
	 *
	 * @param service a service to register
	 * @return a service provider registration to be closed when the local group member no longer provides the specified service.
	 */
	@Override
	ServiceProviderRegistration<T, M> register(T service);

	/**
	 * Registers the local group member as a provider of the specified service, using the specified listener.
	 *
	 * @param service a service to register
	 * @param listener a listener to notify of service provider changes
	 * @return a service provider registration to be closed when the local group member no longer provides the specified service.
	 * @deprecated Use {@link #register(Object, ServiceProviderRegistrationListener)} instead.
	 */
	@Deprecated(forRemoval = true)
	default ServiceProviderRegistration<T, M> register(T service, ServiceProviderListener<M> listener) {
		return this.register(service, new ServiceProviderRegistrationListener<>() {
			@Override
			public void providersChanged(ServiceProviderRegistrationEvent<M> event) {
				listener.providersChanged(event.getCurrentProviders());
			}
		});
	}

	/**
	 * Registers the local group member as a provider of the specified service, using the specified listener.
	 *
	 * @param service a service to register
	 * @param listener a listener to notify of provider changes to the specified service.
	 * @return a service provider registration to be closed when the local group member no longer provides the specified service.
	 */
	default ServiceProviderRegistration<T, M> register(T service, ServiceProviderRegistrationListener<M> listener) {
		Registration listenerRegistration = this.register(new ServiceProviderRegistryListener<>() {
			@Override
			public void providersChanged(T eventService, ServiceProviderRegistrationEvent<M> event) {
				if (service.equals(eventService)) {
					listener.providersChanged(event);
				}
			}
		});
		try {
			ServiceProviderRegistration<T, M> registration = this.register(service);
			return new ServiceProviderRegistration<>() {
				@Override
				public Set<M> getProviders() {
					return registration.getProviders();
				}

				@Override
				public T getService() {
					return registration.getService();
				}

				@Override
				public void close() {
					try {
						registration.close();
					} finally {
						listenerRegistration.close();
					}
				}
			};
		} catch (RuntimeException | Error e) {
			listenerRegistration.close();
			throw e;
		}
	}

	/**
	 * Registers the specified listener for changes to the providers of any service.
	 * @param listener a listener to notify of changes to provisioned services
	 * @return a listener registration
	 */
	Registration register(ServiceProviderRegistryListener<T, M> listener);
}
