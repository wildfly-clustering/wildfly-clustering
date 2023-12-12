/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheContainerConfiguration;

/**
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroupMemberFactory implements CacheContainerGroupMemberFactory {

	private final CacheContainerGroupMember localMember;

	public EmbeddedCacheManagerGroupMemberFactory(EmbeddedCacheContainerConfiguration configuration) {
		this.localMember = new LocalEmbeddedCacheManagerGroupMember(configuration.getCacheContainer().getCacheManagerConfiguration().transport().nodeName());
	}

	@Override
	public CacheContainerGroupMember createGroupMember(Address address) {
		return (address != LocalModeAddress.INSTANCE) ? new EmbeddedCacheManagerGroupMember(address) : this.localMember;
	}
}
