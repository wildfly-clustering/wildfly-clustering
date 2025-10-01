/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.GroupMembershipListener;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMembership;

/**
 * A group associated with a cache container
 * @author Paul Ferraro
 */
public interface CacheContainerGroup extends Group<Address, CacheContainerGroupMember> {
	/**
	 * Returns the cache container associated with this group.
	 * @return the cache container associated with this group.
	 */
	EmbeddedCacheManager getCacheContainer();

	@Override
	CacheContainerGroupMemberFactory getGroupMemberFactory();

	/**
	 * Creates a cache container group for the associated cache.
	 * @param cache an embedded cache
	 * @return a cache container group for the associated cache.
	 */
	default CacheContainerGroup forCache(Cache<?, ?> cache) {
		CacheContainerGroup group = this;
		// If cache is local, return a singleton group
		return cache.getCacheConfiguration().clustering().cacheMode().isClustered() ? group : new CacheContainerGroup() {
			private final GroupMembership<CacheContainerGroupMember> membership = GroupMembership.singleton(group.getLocalMember());

			@Override
			public EmbeddedCacheManager getCacheContainer() {
				return group.getCacheContainer();
			}

			@Override
			public String getName() {
				return group.getName();
			}

			@Override
			public CacheContainerGroupMember getLocalMember() {
				return group.getLocalMember();
			}

			@Override
			public GroupMembership<CacheContainerGroupMember> getMembership() {
				return this.membership;
			}

			@Override
			public boolean isSingleton() {
				return true;
			}

			@Override
			public Registration register(GroupMembershipListener<CacheContainerGroupMember> object) {
				// Membership changes are not relevant to a singleton group
				return Registration.EMPTY;
			}

			@Override
			public CacheContainerGroupMemberFactory getGroupMemberFactory() {
				return group.getGroupMemberFactory();
			}
		};
	}
}
