/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.wildfly.clustering.session.SessionManagerParameters;

/**
 * @author Paul Ferraro
 */
public interface HotRodSessionManagerParameters extends SessionManagerParameters {
	RemoteCacheContainer createRemoteCacheContainer(ConfigurationBuilder builder);
	NearCacheMode getNearCacheMode();
}
