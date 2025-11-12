/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.PersistenceConfiguration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.context.Flag;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.distribution.CacheStreamFilter;
import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.scheduler.Scheduler;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionStatistics;
import org.wildfly.clustering.session.cache.AbstractSessionManager;

/**
 * A session manager implementation backed by an embedded Infinispan cache.
 * @param <DC> the deployment context type
 * @param <MV> the meta-data value type
 * @param <AV> the attributes value type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class InfinispanSessionManager<DC, MV, AV, SC> extends AbstractSessionManager<DC, MV, AV, SC> {

	private final Cache<Key<String>, ?> cache;
	private final CacheProperties properties;
	private final Scheduler<String, ExpirationMetaData> scheduler;

	interface Configuration<DC, MV, AV, SC> extends AbstractSessionManager.Configuration<DC, MV, AV, SC> {
		@Override
		EmbeddedCacheConfiguration getCacheConfiguration();

		@Override
		default Consumer<ImmutableSession> getSessionCloseTask() {
			Scheduler<String, ExpirationMetaData> scheduler = this.getExpirationScheduler();
			return new Consumer<>() {
				@Override
				public void accept(ImmutableSession session) {
					if (session.isValid() && !session.getMetaData().isImmortal()) {
						scheduler.schedule(session.getId(), session.getMetaData());
					}
				}
			};
		}

		@Override
		default Consumer<ImmutableSession> getExpiredSessionHandler() {
			// If we encounter an expired session, remove it via the scheduler
			BiConsumer<String, ImmutableSessionMetaData> schedule = this.getExpirationScheduler()::schedule;
			return schedule.composeUnary(ImmutableSession::getId, ImmutableSession::getMetaData);
		}

		/**
		 * Returns the scheduler used to expire sessions.
		 * @return the scheduler used to expire sessions.
		 */
		Scheduler<String, ExpirationMetaData> getExpirationScheduler();
	}

	/**
	 * Creates a session manager using the specified configuration.
	 * @param configuration the configuration of this session manager
	 */
	public InfinispanSessionManager(Configuration<DC, MV, AV, SC> configuration) {
		super(configuration);
		this.cache = configuration.getCacheConfiguration().getCache();
		this.properties = configuration.getCacheConfiguration().getCacheProperties();
		this.scheduler = configuration.getExpirationScheduler();
	}

	@Override
	public void stop() {
		if (!this.properties.isPersistent()) {
			PersistenceConfiguration persistence = this.cache.getCacheConfiguration().persistence();
			// Don't passivate sessions on stop if we will purge the store on startup
			if (persistence.passivation() && !persistence.stores().stream().allMatch(StoreConfiguration::purgeOnStartup)) {
				// Ensure passivation listeners are triggered prior to stopping
				try (Stream<Key<String>> stream = this.cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL, Flag.SKIP_CACHE_LOAD, Flag.SKIP_LOCKING).keySet().stream()) {
					stream.filter(SessionCacheKeyFilter.META_DATA).forEach(this.cache::evict);
				}
			}
		}
		super.stop();
	}

	@Override
	public CompletionStage<Session<SC>> findSessionAsync(String id) {
		this.scheduler.cancel(id);
		return super.findSessionAsync(id);
	}

	@Override
	public SessionStatistics getStatistics() {
		return this;
	}

	@Override
	public Set<String> getActiveSessions() {
		// Omit passivated sessions
		return getLocalSessions(this.cache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_LOAD));
	}

	@Override
	public Set<String> getSessions() {
		return getLocalSessions(this.cache);
	}

	private static Set<String> getLocalSessions(Cache<Key<String>, ?> cache) {
		CacheStreamFilter<Key<String>> filter = CacheStreamFilter.local(cache);
		try (Stream<Key<String>> keys = filter.apply(cache.keySet().stream())) {
			return keys.filter(SessionCacheKeyFilter.META_DATA).map(Key::getId).collect(Collectors.toUnmodifiableSet());
		}
	}
}
