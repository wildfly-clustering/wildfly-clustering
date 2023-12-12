/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.group.GroupMemberFactory;

/**
 * @author Paul Ferraro
 */
public interface CacheContainerGroupMemberFactory extends GroupMemberFactory<Address, CacheContainerGroupMember> {
}
