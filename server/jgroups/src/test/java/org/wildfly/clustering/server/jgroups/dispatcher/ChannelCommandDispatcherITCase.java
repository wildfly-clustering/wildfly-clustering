/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * @author Paul Ferraro
 */
public class ChannelCommandDispatcherITCase extends CommandDispatcherITCase<ChannelGroupMember> {

	public ChannelCommandDispatcherITCase() {
		super(ChannelCommandDispatcherITCaseConfiguration::new);
	}
}
