/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.infinispan.ChannelEmbeddedCacheManagerGroupConfiguration;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * Encapsulates the configuration of a command dispatcher factory associated with a clustered cache container.
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
				return (address != Address.LOCAL) ? Address.toExtendedUUID(address) : ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration.this.getCommandDispatcherFactory().getGroup().getLocalMember().getId();
			}
		};
	}
}
