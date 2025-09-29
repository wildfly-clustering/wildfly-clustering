/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.LocalEmbeddedCacheManagerGroupConfiguration;
import org.wildfly.clustering.server.local.LocalGroupMember;
import org.wildfly.clustering.server.local.dispatcher.LocalCommandDispatcherFactory;

/**
 * Encapsulates the configuration of a command dispatcher factory associated with a non-clustered cache container.
 * @author Paul Ferraro
 */
public interface LocalEmbeddedCacheManagerCommandDispatcherFactoryConfiguration extends LocalEmbeddedCacheManagerGroupConfiguration, EmbeddedCacheManagerCommandDispatcherFactoryConfiguration<String, LocalGroupMember> {

	@Override
	default GroupCommandDispatcherFactory<String, LocalGroupMember> getCommandDispatcherFactory() {
		return LocalCommandDispatcherFactory.of(this.getGroup());
	}

	@Override
	default java.util.function.Function<Address, String> getAddressUnwrapper() {
		return Function.of(this.getCacheContainer().getCacheManagerConfiguration().transport().nodeName());
	}
}
