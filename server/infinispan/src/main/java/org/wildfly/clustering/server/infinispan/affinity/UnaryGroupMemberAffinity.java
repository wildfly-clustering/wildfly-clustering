/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.affinity;

import java.util.function.Function;

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

	private final KeyDistribution distribution;
	private final CacheContainerGroupMemberFactory factory;

	public UnaryGroupMemberAffinity(GroupMemberAffinityConfiguration<I> configuration) {
		this(configuration.getCache(), configuration.getGroup());
	}

	public UnaryGroupMemberAffinity(Cache<? extends Key<I>, ?> cache, CacheContainerGroup group) {
		this(KeyDistribution.forCache(cache), group.getGroupMemberFactory());
	}

	UnaryGroupMemberAffinity(KeyDistribution distribution, CacheContainerGroupMemberFactory factory) {
		this.distribution = distribution;
		this.factory = factory;
	}

	@Override
	public CacheContainerGroupMember apply(I id) {
		CacheContainerGroupMember member = null;
		while (member == null) {
			Address address = this.distribution.getPrimaryOwner(new CacheKey<>(id));
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
