/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.function.Function;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.wildfly.clustering.cache.function.Functions;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;

/**
 * @author Paul Ferraro
 */
public interface LocalEmbeddedCacheManagerGroupConfiguration extends EmbeddedCacheManagerGroupConfiguration<String, LocalGroupMember> {

	@Override
	default LocalGroup getGroup() {
		GlobalConfiguration global = this.getCacheContainer().getCacheManagerConfiguration();
		return LocalGroup.of(global.cacheManagerName(), global.transport().nodeName());
	}

	@Override
	default Function<String, Address> getAddressWrapper() {
		return Functions.constantFunction(LocalModeAddress.INSTANCE);
	}
}
