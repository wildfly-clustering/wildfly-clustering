/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.jgroups.GroupProvider;

/**
 * @author Paul Ferraro
 */
public interface CacheContainerGroupProvider extends GroupProvider<Address, CacheContainerGroupMember> {

	@Override
	CacheContainerGroup getGroup();

	EmbeddedCacheManager getCacheManager();
}
