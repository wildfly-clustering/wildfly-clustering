/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.util.function.Function;

import org.jgroups.Address;

/**
 * Integration test for JChannel-based group implementation.
 * @author Paul Ferraro
 */
public class JChannelGroupITCase extends GroupITCase<Address, ChannelGroupMember> {

	public JChannelGroupITCase() {
		super(JChannelGroupProvider::new, Function.identity());
	}
}
