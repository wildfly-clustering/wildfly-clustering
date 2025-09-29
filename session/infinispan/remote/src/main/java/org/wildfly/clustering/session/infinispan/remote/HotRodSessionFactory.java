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
import java.util.function.Supplier;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeSessionFactory;
import org.wildfly.clustering.session.cache.attributes.ImmutableSessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;
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
 * @param <MC> the marshalling context type
 * @param <AV> the session attribute entry type
 * @param <SC> the local context type
 */
@ClientListener
public class HotRodSessionFactory<MC, AV, SC> extends CompositeSessionFactory<MC, SessionMetaDataEntry<SC>, AV, SC> {
	private static final System.Logger LOGGER = System.getLogger(HotRodSessionFactory.class.getName());

	private final RemoteCache<SessionCreationMetaDataKey, SessionCreationMetaDataEntry<SC>> creationMetaDataCache;
	private final ImmutableSessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory;
	private final ImmutableSessionAttributesFactory<AV> attributesFactory;
	private final CacheEntryRemover<String> attributesRemover;
	private final Consumer<ImmutableSession> expirationListener;
	private final Executor executor;

	/**
	 * Constructs a new session factory
	 * @param config
	 * @param metaDataFactory
	 * @param attributesFactory
	 * @param localContextFactory
	 * @param expirationListener
	 */
	public HotRodSessionFactory(RemoteCacheConfiguration config, SessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory, SessionAttributesFactory<MC, AV> attributesFactory, Supplier<SC> localContextFactory, Consumer<ImmutableSession> expirationListener) {
		super(metaDataFactory, attributesFactory, config.getCacheProperties(), localContextFactory);
		this.metaDataFactory = metaDataFactory;
		this.attributesFactory = attributesFactory;
		this.attributesRemover = attributesFactory;
		this.expirationListener = expirationListener;
		this.creationMetaDataCache = config.getForceReturnCache();
		this.executor = config.getExecutor();
		this.creationMetaDataCache.addClientListener(this);
	}

	@Override
	public void close() {
		this.creationMetaDataCache.removeClientListener(this);
		super.close();
	}

	@ClientCacheEntryExpired
	public void expired(ClientCacheEntryExpiredEvent<SessionAccessMetaDataKey> event) {
		RemoteCache<SessionCreationMetaDataKey, SessionCreationMetaDataEntry<SC>> creationMetaDataCache = this.creationMetaDataCache;
		ImmutableSessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory = this.metaDataFactory;
		ImmutableSessionAttributesFactory<AV> attributesFactory = this.attributesFactory;
		CacheEntryRemover<String> attributesRemover = this.attributesRemover;
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
						Duration sinceCreation = Duration.between(creationMetaDataEntry.getCreationTime(), Instant.now()).minus(creationMetaDataEntry.getTimeout()).minus(lastAccess);
						accessMetaData.setLastAccessDuration(sinceCreation, lastAccess);

						// Notify session expiration listeners
						ImmutableSessionMetaData metaData = metaDataFactory.createImmutableSessionMetaData(id, new DefaultSessionMetaDataEntry<>(creationMetaDataEntry, accessMetaData));
						Map<String, Object> attributes = attributesFactory.createImmutableSessionAttributes(id, attributesValue);
						ImmutableSession session = HotRodSessionFactory.this.createImmutableSession(id, metaData, attributes);
						LOGGER.log(System.Logger.Level.TRACE, "Session {0} has expired.", id);
						expirationListener.accept(session);
						attributesRemover.remove(id);
					}
				}
			}
		};
		this.executor.execute(task);
	}
}
