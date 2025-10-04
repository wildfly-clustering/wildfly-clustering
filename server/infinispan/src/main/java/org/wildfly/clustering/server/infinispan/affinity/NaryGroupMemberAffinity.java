/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.affinity;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.infinispan.CacheKey;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.KeyDistribution;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMemberFactory;

/**
 * Returns a list of group members that own cache keys for a given identifier.
 * @param <I> the identifier type of a cache key
 * @author Paul Ferraro
 */
public class NaryGroupMemberAffinity<I> implements Function<I, List<CacheContainerGroupMember>> {

	private final Supplier<KeyDistribution> distribution;
	private final CacheContainerGroupMemberFactory factory;
	private final CacheContainerGroupMember localMember;

	/**
	 * Creates a group member affinity function returning multiple values.
	 * @param cache an embedded cache
	 * @param group the cache container group
	 */
	public NaryGroupMemberAffinity(Cache<?, ?> cache, CacheContainerGroup group) {
		this(Supplier.of(cache).thenApply(KeyDistribution::forCache), group.getGroupMemberFactory(), group.getLocalMember());
	}

	NaryGroupMemberAffinity(Supplier<KeyDistribution> distribution, CacheContainerGroupMemberFactory factory, CacheContainerGroupMember localMember) {
		this.distribution = distribution;
		this.factory = factory;
		this.localMember = localMember;
	}

	@Override
	public List<CacheContainerGroupMember> apply(I id) {
		List<CacheContainerGroupMember> members = new LinkedList<>();
		boolean locallyOwned = false;
		for (Address address : this.distribution.get().getOwners(new CacheKey<>(id))) {
			locallyOwned |= this.localMember.getId().equals(address);
			members.add(this.factory.createGroupMember(address));
		}
		if (!locallyOwned) {
			members.add(this.localMember);
		}
		return members;
	}
}
