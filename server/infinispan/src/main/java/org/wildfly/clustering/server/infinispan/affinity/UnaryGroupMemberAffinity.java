/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.affinity;

import java.util.function.Function;
import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.CacheKey;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.KeyDistribution;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMemberFactory;

/**
 * Returns group member that primarily owns cache keys for a given identifier.
 * @param <I> the identifier type of a cache key
 * @author Paul Ferraro
 */
public class UnaryGroupMemberAffinity<I> implements Function<I, CacheContainerGroupMember> {

	private final Supplier<KeyDistribution> distribution;
	private final CacheContainerGroupMemberFactory factory;

	/**
	 * Creates a group member affinity function returning a single value.
	 * @param configuration a group member affinity configuration
	 */
	public UnaryGroupMemberAffinity(GroupMemberAffinityConfiguration<I> configuration) {
		this(configuration.getCache(), configuration.getGroup());
	}

	private UnaryGroupMemberAffinity(Cache<? extends Key<I>, ?> cache, CacheContainerGroup group) {
		this(new Supplier<>() {
			@Override
			public KeyDistribution get() {
				return KeyDistribution.forCache(cache);
			}
		}, group.getGroupMemberFactory());
	}

	UnaryGroupMemberAffinity(Supplier<KeyDistribution> distribution, CacheContainerGroupMemberFactory factory) {
		this.distribution = distribution;
		this.factory = factory;
	}

	@Override
	public CacheContainerGroupMember apply(I id) {
		CacheContainerGroupMember member = null;
		while (member == null) {
			Address address = this.distribution.get().getPrimaryOwner(new CacheKey<>(id));
			// This has been observed to return null mid-rebalance
			if (address != null) {
				// This can return null if member has left the cluster
				member = this.factory.createGroupMember(address);
			} else {
				Thread.yield();
			}
		}
		return member;
	}
}
