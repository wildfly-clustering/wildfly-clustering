/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.infinispan.ChannelEmbeddedCacheManagerGroupConfiguration;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * @author Paul Ferraro
 */
public interface ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration extends ChannelEmbeddedCacheManagerGroupConfiguration, EmbeddedCacheManagerCommandDispatcherFactoryConfiguration<org.jgroups.Address, ChannelGroupMember> {

	@Override
	default Group<org.jgroups.Address, ChannelGroupMember> getGroup() {
		return this.getCommandDispatcherFactory().getGroup();
	}

	@Override
	default Function<Address, org.jgroups.Address> getAddressUnwrapper() {
		return new Function<>() {
			@Override
			public org.jgroups.Address apply(Address address) {
				return JGroupsAddress.class.cast(address).getJGroupsAddress();
			}
		};
	}
}
