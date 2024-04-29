/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import jakarta.transaction.TransactionManager;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.commons.IllegalLifecycleStateException;
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

	@Override
	default Executor getExecutor() {
		@SuppressWarnings("removal")
		Executor executor = this.getCache().getRemoteCacheManager().getAsyncExecutorService();
		return new Executor() {
			@Override
			public void execute(Runnable command) {
				try {
					executor.execute(command);
				} catch (IllegalLifecycleStateException e) {
					throw new RejectedExecutionException(e);
				}
			}
		};
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
