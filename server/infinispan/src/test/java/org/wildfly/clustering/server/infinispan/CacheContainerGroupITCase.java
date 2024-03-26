/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.wildfly.clustering.server.jgroups.GroupITCase;

/**
 * Integration test for JChannel-based group implementation.
 * @author Paul Ferraro
 */
public class CacheContainerGroupITCase extends GroupITCase<Address, CacheContainerGroupMember> {
	private static final Function<Address, JGroupsAddress> CAST = JGroupsAddress.class::cast;

	public CacheContainerGroupITCase() {
		super(EmbeddedCacheManagerGroupProvider::new, CAST.andThen(JGroupsAddress::getJGroupsAddress));
	}
}
