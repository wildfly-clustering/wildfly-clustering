/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.Address;
import org.wildfly.clustering.server.group.GroupMemberFactory;

/**
 * @author Paul Ferraro
 */
public interface ChannelGroupMemberFactory extends GroupMemberFactory<Address, ChannelGroupMember> {

}
