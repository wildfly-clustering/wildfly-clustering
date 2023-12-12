/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import org.infinispan.Cache;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.remoting.transport.Address;

/**
 * {@link Locality} implementation based on a {@link ConsistentHash}.
 * @author Paul Ferraro
 */
public class ConsistentHashLocality implements Locality {

	private final KeyDistribution distribution;
	private final Address localAddress;

	ConsistentHashLocality(Cache<?, ?> cache, ConsistentHash hash) {
		this(KeyDistribution.forConsistentHash(cache, hash), cache.getCacheManager().getAddress());
	}

	ConsistentHashLocality(KeyDistribution distribution, Address localAddress) {
		this.distribution = distribution;
		this.localAddress = localAddress;
	}

	@Override
	public boolean isLocal(Object key) {
		Address primary = this.distribution.getPrimaryOwner(key);
		return this.localAddress.equals(primary);
	}
}
