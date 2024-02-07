/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.jupiter.api.extension.Extension;

/**
 * JUnit extension that configures a {@link RemoteCacheContainer} factory.
 * @author Paul Ferraro
 */
public interface RemoteCacheContainerConfigurator extends Extension {

	Configuration configure(ConfigurationBuilder builder);
}
