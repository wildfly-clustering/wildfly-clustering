/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.concurrent.Executor;

import jakarta.transaction.TransactionManager;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.BasicCacheConfiguration;

/**
 * Configuration identifying a remote cache.
 * @author Paul Ferraro
 */
public interface RemoteCacheConfiguration extends RemoteCacheContainerConfiguration, BasicCacheConfiguration {

	@Override
	<CK, CV> RemoteCache<CK, CV> getCache();

	@Override
	default RemoteCacheContainer getCacheContainer() {
		return this.getCache().getRemoteCacheContainer();
	}

	@SuppressWarnings("removal")
	@Override
	default Executor getExecutor() {
		return this.getCache().getRemoteCacheManager().getAsyncExecutorService();
	}

	@Override
	default TransactionManager getTransactionManager() {
		return this.getCache().getTransactionManager();
	}

	default Flag[] getIgnoreReturnFlags() {
		return this.getNearCacheMode().enabled() ? new Flag[0] : new Flag[] { Flag.SKIP_LISTENER_NOTIFICATION };
	}

	default Flag[] getForceReturnFlags() {
		return this.getNearCacheMode().enabled() ? new Flag[] { Flag.FORCE_RETURN_VALUE } : new Flag[] { Flag.FORCE_RETURN_VALUE, Flag.SKIP_LISTENER_NOTIFICATION };
	}

	default NearCacheMode getNearCacheMode() {
		RemoteCache<?, ?> cache = this.getCache();
		return cache.getRemoteCacheContainer().getConfiguration().remoteCaches().get(cache.getName()).nearCacheMode();
	}

	@Override
	default CacheProperties getCacheProperties() {
		return new RemoteCacheProperties(this.getCache());
	}
}
