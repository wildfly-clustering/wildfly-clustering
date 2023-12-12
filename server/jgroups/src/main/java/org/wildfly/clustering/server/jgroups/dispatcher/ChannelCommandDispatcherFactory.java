/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import org.jgroups.Address;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.ChannelGroup;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * @author Paul Ferraro
 */
public interface ChannelCommandDispatcherFactory extends GroupCommandDispatcherFactory<Address, ChannelGroupMember>, Registration {

	@Override
	ChannelGroup getGroup();
}
