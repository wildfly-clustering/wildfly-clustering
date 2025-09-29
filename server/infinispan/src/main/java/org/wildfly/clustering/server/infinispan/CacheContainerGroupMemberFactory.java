/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.group.GroupMemberFactory;

/**
 * A factory that creates cache container-based group members.
 * @author Paul Ferraro
 */
public interface CacheContainerGroupMemberFactory extends GroupMemberFactory<Address, CacheContainerGroupMember> {
}
