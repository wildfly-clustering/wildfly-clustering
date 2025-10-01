/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;

/**
 * A command dispatcher factory associated with a cache container.
 * @author Paul Ferraro
 */
public interface CacheContainerCommandDispatcherFactory extends GroupCommandDispatcherFactory<Address, CacheContainerGroupMember> {

	@Override
	CacheContainerGroup getGroup();
}
