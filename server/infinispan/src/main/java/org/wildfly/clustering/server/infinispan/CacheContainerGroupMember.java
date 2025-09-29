/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * A cache container-based group member.
 * @author Paul Ferraro
 */
public interface CacheContainerGroupMember extends GroupMember<Address> {
}
