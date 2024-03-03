/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionAttributes;
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
public class HotRodSessionFactory<MC, AV, SC> extends CompositeSessionFactory<MC, SessionMetaDataEntry<SC>, AV, SC> implements Registrar<Consumer<ImmutableSession>> {
	private static final Logger LOGGER = Logger.getLogger(HotRodSessionFactory.class);

	private final RemoteCache<SessionCreationMetaDataKey, SessionCreationMetaDataEntry<SC>> creationMetaDataCache;
	private final Flag[] forceReturnFlags;
	private final ImmutableSessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory;
	private final ImmutableSessionAttributesFactory<AV> attributesFactory;
	private final Remover<String> attributesRemover;
	private final Collection<Consumer<ImmutableSession>> listeners = new CopyOnWriteArraySet<>();
	private final Executor executor;

	/**
	 * Constructs a new session factory
	 * @param config
	 * @param metaDataFactory
	 * @param attributesFactory
	 * @param localContextFactory
	 */
	public HotRodSessionFactory(RemoteCacheConfiguration config, SessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory, SessionAttributesFactory<MC, AV> attributesFactory, Supplier<SC> localContextFactory) {
		super(metaDataFactory, attributesFactory, localContextFactory);
		this.metaDataFactory = metaDataFactory;
		this.attributesFactory = attributesFactory;
		this.attributesRemover = attributesFactory;
		this.creationMetaDataCache = config.getCache();
		this.forceReturnFlags = config.getForceReturnFlags();
		this.executor = config.getExecutor();
		this.creationMetaDataCache.addClientListener(this);
	}

	@Override
	public void close() {
		this.creationMetaDataCache.removeClientListener(this);
	}

	@ClientCacheEntryExpired
	public void expired(ClientCacheEntryExpiredEvent<SessionAccessMetaDataKey> event) {
		RemoteCache<SessionCreationMetaDataKey, SessionCreationMetaDataEntry<SC>> creationMetaDataCache = this.creationMetaDataCache;
		Flag[] forceReturnFlags = this.forceReturnFlags;
		ImmutableSessionMetaDataFactory<SessionMetaDataEntry<SC>> metaDataFactory = this.metaDataFactory;
		ImmutableSessionAttributesFactory<AV> attributesFactory = this.attributesFactory;
		Remover<String> attributesRemover = this.attributesRemover;
		Collection<Consumer<ImmutableSession>> listeners = this.listeners;
		String id = event.getKey().getId();
		Runnable task = new Runnable() {
			@Override
			public void run() {
				SessionCreationMetaDataEntry<SC> creationMetaDataEntry = creationMetaDataCache.withFlags(forceReturnFlags).remove(new SessionCreationMetaDataKey(id));
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
						ImmutableSessionAttributes attributes = attributesFactory.createImmutableSessionAttributes(id, attributesValue);
						ImmutableSession session = HotRodSessionFactory.this.createImmutableSession(id, metaData, attributes);
						LOGGER.tracef("Session %s has expired.", id);
						for (Consumer<ImmutableSession> listener : listeners) {
							listener.accept(session);
						}
						attributesRemover.remove(id);
					}
				}
			}
		};
		this.executor.execute(task);
	}

	@Override
	public Registration register(Consumer<ImmutableSession> listener) {
		this.listeners.add(listener);
		return () -> this.listeners.remove(listener);
	}
}
