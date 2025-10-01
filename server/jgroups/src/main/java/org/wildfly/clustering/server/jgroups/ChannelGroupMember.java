/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.Address;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * A channel-based group member.
 * @author Paul Ferraro
 */
public interface ChannelGroupMember extends GroupMember<Address> {

}
