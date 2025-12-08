/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeSessionFactory;
import org.wildfly.clustering.session.cache.SessionFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.fine.DefaultSessionAccessMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.DefaultSessionMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.SessionAccessMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.SessionCreationMetaDataEntry;
import org.wildfly.clustering.session.cache.metadata.fine.SessionMetaDataEntry;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionAccessMetaDataKey;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionCreationMetaDataKey;

/**
 * Factory for creating a {@link org.wildfly.clustering.session.Session} backed by a set of {@link RemoteCache} entries.
 * @author Paul Ferraro
 * @param <DC> the deployment context type
 * @param <AV> the session attribute entry type
 * @param <SC> the local context type
 */
@ClientListener
public class HotRodSessionFactory<DC, AV, SC> extends CompositeSessionFactory<DC, SessionMetaDataEntry<SC>, AV, SC> {
	private static final System.Logger LOGGER = System.getLogger(HotRodSessionFactory.class.getName());

	interface Configuration<DC, AV, SC> extends SessionFactoryConfiguration<DC, SessionMetaDataEntry<SC>, AV, SC> {
		RemoteCacheConfiguration getCacheConfiguration();

		@Override
		default CacheProperties getCacheProperties() {
			return this.getCacheConfiguration().getCacheProperties();
		}

		Consumer<ImmutableSession> getSessionExpirationListener();
	}

	private final RemoteCache<SessionCreationMetaDataKey, SessionCreationMetaDataEntry<SC>> creationMetaDataCache;
	private final ImmutableSessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory;
	private final SessionAttributesFactory<DC, AV> attributesFactory;
	private final Consumer<ImmutableSession> expirationListener;
	private final Executor executor;

	/**
	 * Constructs a new session factory.
	 * @param configuration the configuration of this session factory
	 */
	public HotRodSessionFactory(Configuration<DC, AV, SC> configuration) {
		super(configuration);
		this.metaDataFactory = configuration.getSessionMetaDataFactory();
		this.attributesFactory = configuration.getSessionAttributesFactory();
		this.expirationListener = configuration.getSessionExpirationListener();
		this.creationMetaDataCache = configuration.getCacheConfiguration().getForceReturnCache();
		this.executor = configuration.getCacheConfiguration().getExecutor();
		this.creationMetaDataCache.addClientListener(this);
	}

	@Override
	public void close() {
		this.creationMetaDataCache.removeClientListener(this);
		super.close();
	}

	/**
	 * Handles expiration events from the remote cluster.
	 * @param event a cache entry expiration event
	 */
	@ClientCacheEntryExpired
	public void expired(ClientCacheEntryExpiredEvent<SessionAccessMetaDataKey> event) {
		RemoteCache<SessionCreationMetaDataKey, SessionCreationMetaDataEntry<SC>> creationMetaDataCache = this.creationMetaDataCache;
		ImmutableSessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory = this.metaDataFactory;
		SessionAttributesFactory<DC, AV> attributesFactory = this.attributesFactory;
		Consumer<ImmutableSession> expirationListener = this.expirationListener;
		String id = event.getKey().getId();
		Runnable task = new Runnable() {
			@Override
			public void run() {
				SessionCreationMetaDataEntry<SC> creationMetaDataEntry = creationMetaDataCache.remove(new SessionCreationMetaDataKey(id));
				if (creationMetaDataEntry != null) {
					AV attributesValue = attributesFactory.findValue(id);
					if (attributesValue != null) {
						// Fabricate a reasonable SessionAccessMetaData
						SessionAccessMetaDataEntry accessMetaData = new DefaultSessionAccessMetaDataEntry();
						Duration lastAccess = Duration.ofSeconds(1);
						Duration sinceCreation = Duration.between(creationMetaDataEntry.getCreationTime(), Instant.now()).minus(creationMetaDataEntry.getMaxIdle()).minus(lastAccess);
						accessMetaData.setLastAccessDuration(sinceCreation, lastAccess);

						// Notify session expiration listeners
						ImmutableSessionMetaData metaData = metaDataFactory.createImmutableSessionMetaData(id, new DefaultSessionMetaDataEntry<>(creationMetaDataEntry, accessMetaData));
						Map<String, Object> attributes = attributesFactory.createImmutableSessionAttributes(id, attributesValue);
						ImmutableSession session = HotRodSessionFactory.this.createImmutableSession(id, metaData, attributes);
						LOGGER.log(System.Logger.Level.TRACE, "Session {0} has expired.", id);
						expirationListener.accept(session);
						attributesFactory.remove(id);
					}
				}
			}
		};
		this.executor.execute(task);
	}
}
