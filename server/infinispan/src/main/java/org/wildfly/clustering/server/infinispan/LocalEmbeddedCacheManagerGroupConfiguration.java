/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;

/**
 * Encapsulates a configuration of a group associated with a non-clustered cache container.
 * @author Paul Ferraro
 */
public interface LocalEmbeddedCacheManagerGroupConfiguration extends EmbeddedCacheManagerGroupConfiguration<String, LocalGroupMember> {

	@Override
	default LocalGroup getGroup() {
		GlobalConfiguration global = this.getCacheContainer().getCacheManagerConfiguration();
		return LocalGroup.of(global.cacheManagerName(), global.transport().nodeName());
	}

	@Override
	default java.util.function.Function<String, Address> getAddressWrapper() {
		return Function.of(LocalModeAddress.INSTANCE);
	}
}
