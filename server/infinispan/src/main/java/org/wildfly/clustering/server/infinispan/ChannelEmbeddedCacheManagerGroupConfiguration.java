/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.AddressCache;
import org.jgroups.util.ExtendedUUID;
import org.jgroups.util.UUID;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * Encapsulates a configuration of a group associated with a clustered cache container.
 * @author Paul Ferraro
 */
public interface ChannelEmbeddedCacheManagerGroupConfiguration extends EmbeddedCacheManagerGroupConfiguration<org.jgroups.Address, ChannelGroupMember> {

	@Override
	default Function<org.jgroups.Address, Address> getAddressWrapper() {
		return new Function<>() {
			@Override
			public Address apply(org.jgroups.Address address) {
				if (address == null) {
					return Address.LOCAL;
				}
				if (address instanceof ExtendedUUID uuid) {
					return AddressCache.fromExtendedUUID(uuid);
				}
				if (address instanceof UUID uuid) {
					return Address.fromNodeUUID(new java.util.UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
				}
				throw new IllegalArgumentException(address.toString());
			}
		};
	}
}
