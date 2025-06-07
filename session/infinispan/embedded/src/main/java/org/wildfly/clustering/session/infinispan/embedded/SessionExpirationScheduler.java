/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.context.DefaultThreadFactory;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.scheduler.AbstractCacheEntryScheduler;
import org.wildfly.clustering.server.local.scheduler.LocalScheduler;
import org.wildfly.clustering.server.local.scheduler.LocalSchedulerConfiguration;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * Session expiration scheduler that eagerly expires sessions as soon as they are eligible.
 * @author Paul Ferraro
 * @param <K> the cache key type
 * @param <V> the cache value type
 */
public class SessionExpirationScheduler<K extends Key<String>, V> extends AbstractCacheEntryScheduler<String, K, V, ExpirationMetaData> {
	private static final System.Logger LOGGER = System.getLogger(SessionExpirationScheduler.class.getName());
	@SuppressWarnings("removal")
	private static final ThreadFactory THREAD_FACTORY = new DefaultThreadFactory(SessionExpirationScheduler.class, AccessController.doPrivileged(new PrivilegedAction<>() {
		@Override
		public ClassLoader run() {
			return SessionExpirationScheduler.class.getClassLoader();
		}
	}));

	private final ImmutableSessionMetaDataFactory<V> metaDataFactory;

	public SessionExpirationScheduler(String name, Supplier<Batch> batchFactory, ImmutableSessionMetaDataFactory<V> metaDataFactory, Predicate<String> remover, Duration closeTimeout) {
		this(new LocalSchedulerConfiguration<>() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public Predicate<String> getTask() {
				return new SessionRemoveTask(batchFactory, remover);
			}

			@Override
			public Duration getCloseTimeout() {
				return closeTimeout;
			}

			@Override
			public ThreadFactory getThreadFactory() {
				return THREAD_FACTORY;
			}
		}, metaDataFactory);
	}

	public SessionExpirationScheduler(LocalSchedulerConfiguration<String> config, ImmutableSessionMetaDataFactory<V> metaDataFactory) {
		this(new LocalScheduler<>(config), metaDataFactory);
	}

	public SessionExpirationScheduler(Scheduler<String, Instant> scheduler, ImmutableSessionMetaDataFactory<V> metaDataFactory) {
		super(scheduler.map(ExpirationMetaData::getExpirationTime));
		this.metaDataFactory = metaDataFactory;
	}

	@Override
	public void schedule(String id) {
		V value = this.metaDataFactory.findValue(id);
		if (value != null) {
			this.schedule(id, value);
		}
	}

	@Override
	public void schedule(Map.Entry<K, V> entry) {
		this.schedule(entry.getKey().getId(), entry.getValue());
	}

	private void schedule(String id, V value) {
		ImmutableSessionMetaData metaData = this.metaDataFactory.createImmutableSessionMetaData(id, value);
		if (!metaData.isImmortal()) {
			this.schedule(id, metaData);
		}
	}

	private static class SessionRemoveTask implements Predicate<String> {
		private final Supplier<Batch> batchFactory;
		private final Predicate<String> remover;

		SessionRemoveTask(Supplier<Batch> batchFactory, Predicate<String> remover) {
			this.batchFactory = batchFactory;
			this.remover = remover;
		}

		@Override
		public boolean test(String id) {
			LOGGER.log(System.Logger.Level.DEBUG, "Expiring session {0}", id);
			try (Batch batch = this.batchFactory.get()) {
				try {
					return this.remover.test(id);
				} catch (RuntimeException | Error e) {
					batch.discard();
					throw e;
				}
			} catch (RuntimeException | Error e) {
				LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
				return false;
			}
		}
	}
}
