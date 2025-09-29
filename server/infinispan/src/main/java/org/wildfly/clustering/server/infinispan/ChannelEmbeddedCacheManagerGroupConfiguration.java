/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddressCache;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * Encapsulates a configuration of a group associated with a clustered cache container.
 * @author Paul Ferraro
 */
public interface ChannelEmbeddedCacheManagerGroupConfiguration extends EmbeddedCacheManagerGroupConfiguration<org.jgroups.Address, ChannelGroupMember> {

	@Override
	default Function<org.jgroups.Address, Address> getAddressWrapper() {
		return JGroupsAddressCache::fromJGroupsAddress;
	}
}
