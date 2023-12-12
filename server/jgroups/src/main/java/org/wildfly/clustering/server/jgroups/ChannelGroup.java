/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.Address;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.Group;

/**
 * @author Paul Ferraro
 */
public interface ChannelGroup extends Group<Address, ChannelGroupMember>, Registration {

	@Override
	ChannelGroupMemberFactory getGroupMemberFactory();
}
