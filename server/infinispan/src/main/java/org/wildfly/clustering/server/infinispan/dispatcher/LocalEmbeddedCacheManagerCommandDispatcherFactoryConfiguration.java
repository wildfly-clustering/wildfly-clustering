/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.function.Functions;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.LocalEmbeddedCacheManagerGroupConfiguration;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.local.dispatcher.LocalCommandDispatcherFactory;

/**
 * @author Paul Ferraro
 */
public interface LocalEmbeddedCacheManagerCommandDispatcherFactoryConfiguration extends LocalEmbeddedCacheManagerGroupConfiguration, EmbeddedCacheManagerCommandDispatcherFactoryConfiguration<String, LocalGroupMember> {

	@Override
	default GroupCommandDispatcherFactory<String, LocalGroupMember> getCommandDispatcherFactory() {
		return LocalCommandDispatcherFactory.of(this.getGroup());
	}

	@Override
	default Function<Address, String> getAddressUnwrapper() {
		return Functions.constantFunction(this.getCacheContainer().getCacheManagerConfiguration().transport().nodeName());
	}
}
