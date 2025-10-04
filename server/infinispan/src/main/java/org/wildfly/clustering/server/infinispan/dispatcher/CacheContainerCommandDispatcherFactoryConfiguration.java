/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupConfiguration;

/**
 * Configuration requiring a command dispatcher factory.
 * @author Paul Ferraro
 */
public interface CacheContainerCommandDispatcherFactoryConfiguration extends CacheContainerGroupConfiguration {
	/**
	 * Returns the command dispatcher factory associated with this configuration.
	 * @return the command dispatcher factory associated with this configuration.
	 */
	CacheContainerCommandDispatcherFactory getCommandDispatcherFactory();

	@Override
	default CacheContainerGroup getGroup() {
		return this.getCommandDispatcherFactory().getGroup();
	}
}
