/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheContainerConfigurator;
import org.wildfly.clustering.session.SessionManagerParameters;

/**
 * @author Paul Ferraro
 */
public interface HotRodSessionManagerParameters extends SessionManagerParameters {
	NearCacheMode getNearCacheMode();
	RemoteCacheContainerConfigurator getRemoteCacheContainerConfigurator();
}
