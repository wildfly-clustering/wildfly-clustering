/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.affinity;

import java.util.function.Function;

import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.infinispan.CacheKey;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.KeyDistribution;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.group.GroupMemberFactory;

/**
 * @author Paul Ferraro
 */
public class UnaryGroupMemberAffinity<I, M extends GroupMember<Address>> implements Function<I, M> {

	private final KeyDistribution distribution;
	private final GroupMemberFactory<Address, M> factory;

	public UnaryGroupMemberAffinity(GroupMemberAffinityConfiguration<I, M> configuration) {
		this(configuration.getCache(), configuration.getGroup());
	}

	public UnaryGroupMemberAffinity(Cache<? extends CacheKey<I>, ?> cache, Group<Address, M> group) {
		this(KeyDistribution.forCache(cache), group.getGroupMemberFactory());
	}

	UnaryGroupMemberAffinity(KeyDistribution distribution, GroupMemberFactory<Address, M> factory) {
		this.distribution = distribution;
		this.factory = factory;
	}

	@Override
	public M apply(I id) {
		M member = null;
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
