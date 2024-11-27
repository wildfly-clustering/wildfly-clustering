/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.context.DefaultThreadFactory;
import org.wildfly.clustering.server.infinispan.expiration.AbstractExpirationScheduler;
import org.wildfly.clustering.server.local.scheduler.LocalScheduler;
import org.wildfly.clustering.server.local.scheduler.LocalSchedulerConfiguration;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * Session expiration scheduler that eagerly expires sessions as soon as they are eligible.
 * If/When Infinispan implements expiration notifications (ISPN-694), this will be obsolete.
 * @author Paul Ferraro
 * @param <MV> the meta data value type
 */
public class SessionExpirationScheduler<MV> extends AbstractExpirationScheduler<String> {
	private static final Logger LOGGER = Logger.getLogger(SessionExpirationScheduler.class);
	private static final ThreadFactory THREAD_FACTORY = new DefaultThreadFactory(SessionExpirationScheduler.class, AccessController.doPrivileged(new PrivilegedAction<>() {
		@Override
		public ClassLoader run() {
			return SessionExpirationScheduler.class.getClassLoader();
		}
	}));

	private final ImmutableSessionMetaDataFactory<MV> metaDataFactory;

	public SessionExpirationScheduler(String name, Supplier<Batch> batchFactory, ImmutableSessionMetaDataFactory<MV> metaDataFactory, Predicate<String> remover, Duration closeTimeout) {
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

	public SessionExpirationScheduler(LocalSchedulerConfiguration<String> config, ImmutableSessionMetaDataFactory<MV> metaDataFactory) {
		this(new LocalScheduler<>(config), metaDataFactory);
	}

	public SessionExpirationScheduler(Scheduler<String, Instant> scheduler, ImmutableSessionMetaDataFactory<MV> metaDataFactory) {
		super(scheduler);
		this.metaDataFactory = metaDataFactory;
	}

	@Override
	public void schedule(String id) {
		MV value = this.metaDataFactory.findValue(id);
		if (value != null) {
			ImmutableSessionMetaData metaData = this.metaDataFactory.createImmutableSessionMetaData(id, value);
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
			LOGGER.debugf("Expiring session %s", id);
			try (Batch batch = this.batchFactory.get()) {
				try {
					return this.remover.test(id);
				} catch (RuntimeException e) {
					batch.discard();
					throw e;
				}
			} catch (RuntimeException e) {
				LOGGER.warnf(e, id.toString());
				return false;
			}
		}
	}
}
