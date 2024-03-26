/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.registry;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.registry.RegistryFactory;

/**
 * An embedded Infinispan registry factory.
 * @author Paul Ferraro
 * @param <K> the registry key type
 * @param <V> the registry value type
 */
public interface CacheContainerRegistryFactory<K, V> extends RegistryFactory<CacheContainerGroupMember, K, V> {

	@Override
	CacheContainerRegistry<K, V> createRegistry(Map.Entry<K, V> entry);

	/**
	 * Returns a singleton registry factory that only permits a single registry entry per instance.
	 * @param <K> the registry key type
	 * @param <V> the registry value type
	 * @param factory a registry factory creating a registry from an entry and close task
	 * @return a singleton registry factory
	 */
	static <K, V> CacheContainerRegistryFactory<K, V> singleton(BiFunction<Map.Entry<K, V>, Runnable, CacheContainerRegistry<K, V>> factory) {
		AtomicReference<Map.Entry<K, V>> reference = new AtomicReference<>();
		return new CacheContainerRegistryFactory<>() {
			@Override
			public CacheContainerRegistry<K, V> createRegistry(Map.Entry<K, V> entry) {
				// Ensure only one registry is created at a time
				if (!reference.compareAndSet(null, entry)) {
					throw new IllegalStateException();
				}
				return factory.apply(entry, () -> reference.set(null));
			}
		};
	}
}
