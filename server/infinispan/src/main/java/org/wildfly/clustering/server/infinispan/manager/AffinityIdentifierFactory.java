/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan.manager;

import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.affinity.KeyAffinityService;
import org.infinispan.affinity.KeyGenerator;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.CacheKey;
import org.wildfly.clustering.cache.infinispan.embedded.affinity.KeyAffinityServiceFactory;
import org.wildfly.clustering.server.manager.IdentifierFactory;

/**
 * An {@link IdentifierFactory} that uses a {@link KeyAffinityService} to pre-generate locally hashing identifiers from a supplier.
 * @author Paul Ferraro
 * @param <I> the identifier type
 */
public class AffinityIdentifierFactory<I> implements IdentifierFactory<I>, KeyGenerator<Key<I>> {

	private final Supplier<I> factory;
	private final KeyAffinityService<? extends Key<I>> affinity;
	private final Address localAddress;

	public AffinityIdentifierFactory(Supplier<I> factory, Cache<? extends Key<I>, ?> cache) {
		this.factory = factory;
		this.affinity = KeyAffinityServiceFactory.INSTANCE.createService(cache, this);
		this.localAddress = cache.getCacheManager().getAddress();
	}

	@Override
	public I get() {
		return this.affinity.getKeyForAddress(this.localAddress).getId();
	}

	@Override
	public Key<I> getKey() {
		return new CacheKey<>(this.factory.get());
	}

	@Override
	public boolean isStarted() {
		return this.affinity.isStarted();
	}

	@Override
	public void start() {
		this.affinity.start();
	}

	@Override
	public void stop() {
		this.affinity.stop();
	}
}
