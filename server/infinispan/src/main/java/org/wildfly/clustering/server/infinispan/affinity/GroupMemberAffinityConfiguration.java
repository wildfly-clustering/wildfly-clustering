/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.affinity;

import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.infinispan.CacheKey;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public interface GroupMemberAffinityConfiguration<I, M extends GroupMember<Address>> {

	Cache<? extends CacheKey<I>, ?> getCache();

	Group<Address, M> getGroup();
}
