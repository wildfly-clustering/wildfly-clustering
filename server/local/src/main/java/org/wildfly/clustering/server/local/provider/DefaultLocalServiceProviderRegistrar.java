/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.provider;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.wildfly.clustering.context.DefaultExecutorService;
import org.wildfly.clustering.context.DefaultThreadFactory;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistration;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationEvent;
import org.wildfly.clustering.server.provider.ServiceProviderRegistrationListener;

/**
 * Local service provider registrar.
 * @author Paul Ferraro
 * @param <T> the service type
 */
public class DefaultLocalServiceProviderRegistrar<T> implements LocalServiceProviderRegistrar<T> {
	private final LocalGroup group;
	private final Set<T> services = ConcurrentHashMap.newKeySet();

	/**
	 * Constructs a service provider registrar for the specified local group.
	 * @param group a local group
	 */
	public DefaultLocalServiceProviderRegistrar(LocalGroup group) {
		this.group = group;
	}

	@Override
	public LocalGroup getGroup() {
		return this.group;
	}

	@Override
	public Set<LocalGroupMember> getProviders(T service) {
		return this.services.contains(service) ? Set.of(this.group.getLocalMember()) : Set.of();
	}

	@Override
	public Set<T> getServices() {
		return Collections.unmodifiableSet(this.services);
	}

	@Override
	public ServiceProviderRegistration<T, LocalGroupMember> register(T service, ServiceProviderRegistrationListener<LocalGroupMember> listener) {
		this.services.add(service);
		Set<LocalGroupMember> members = this.getProviders(service);
		@SuppressWarnings("removal")
		ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return Thread.currentThread().getContextClassLoader();
			}
		});
		@SuppressWarnings("removal")
		ThreadFactory threadFactory = AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ThreadFactory run() {
				return new DefaultThreadFactory(DefaultLocalServiceProviderRegistrar.class, listener.getClass().getClassLoader());
			}
		});
		ExecutorService executor = new DefaultExecutorService(Executors.newSingleThreadExecutor(threadFactory), loader);
		notify(executor, listener, Set.of(), members);
		return new DefaultServiceProviderRegistration<>(this, service, () -> {
			this.services.remove(service);
			try {
				notify(executor, listener, members, Set.of());
			} finally {
				executor.shutdown();
			}
		});
	}

	private static void notify(Executor executor, ServiceProviderRegistrationListener<LocalGroupMember> listener, Set<LocalGroupMember> previousProviders, Set<LocalGroupMember> currentProviders) {
		executor.execute(() -> listener.providersChanged(new ServiceProviderRegistrationEvent<>() {
			@Override
			public Set<LocalGroupMember> getPreviousProviders() {
				return previousProviders;
			}

			@Override
			public Set<LocalGroupMember> getCurrentProviders() {
				return currentProviders;
			}
		}));
	}
}
