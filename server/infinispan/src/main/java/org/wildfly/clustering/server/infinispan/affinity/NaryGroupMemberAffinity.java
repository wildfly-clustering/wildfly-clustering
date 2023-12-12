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
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.group.GroupMemberFactory;

/**
 * @author Paul Ferraro
 */
public class NaryGroupMemberAffinity<I, M extends GroupMember<Address>> implements Function<I, List<M>> {

	private final KeyDistribution distribution;
	private final GroupMemberFactory<Address, M> factory;
	private final M localMember;

	public NaryGroupMemberAffinity(GroupMemberAffinityConfiguration<I, M> configuration) {
		this(configuration.getCache(), configuration.getGroup());
	}

	public NaryGroupMemberAffinity(Cache<? extends CacheKey<I>, ?> cache, Group<Address, M> group) {
		this(KeyDistribution.forCache(cache), group.getGroupMemberFactory(), group.getLocalMember());
	}

	NaryGroupMemberAffinity(KeyDistribution distribution, GroupMemberFactory<Address, M> factory, M localMember) {
		this.distribution = distribution;
		this.factory = factory;
		this.localMember = localMember;
	}

	@Override
	public List<M> apply(I id) {
		List<M> members = new LinkedList<>();
		boolean locallyOwned = false;
		for (Address address : this.distribution.getOwners(new CacheKey<>(id))) {
			locallyOwned |= this.localMember.getAddress().equals(address);
			members.add(this.factory.createGroupMember(address));
		}
		if (!locallyOwned) {
			members.add(this.localMember);
		}
		return members;
	}
}
