/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.attributes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.infinispan.Cache;
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheMutatorFactory;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.cache.infinispan.embedded.listener.PostActivateBlockingListener;
import org.wildfly.clustering.cache.infinispan.embedded.listener.PostPassivateBlockingListener;
import org.wildfly.clustering.cache.infinispan.embedded.listener.PrePassivateBlockingListener;
import org.wildfly.clustering.marshalling.Marshallability;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeImmutableSession;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.coarse.CoarseSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.coarse.SessionActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;
import org.wildfly.common.function.Functions;

/**
 * {@link SessionAttributesFactory} for coarse granularity sessions, where all session attributes are stored in a single cache entry.
 * This implementation is safe for attributes that strongly reference each other.
 * @param <C> the session manager context type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class CoarseSessionAttributesFactory<C, V> implements SessionAttributesFactory<C, Map<String, Object>> {
	private static final Logger LOGGER = Logger.getLogger(CoarseSessionAttributesFactory.class);

	private final Cache<SessionAttributesKey, V> cache;
	private final Cache<SessionAttributesKey, V> writeCache;
	private final Cache<SessionAttributesKey, V> silentCache;
	private final Marshaller<Map<String, Object>, V> marshaller;
	private final CacheProperties properties;
	private final Immutability immutability;
	private final CacheEntryMutatorFactory<SessionAttributesKey, V> mutatorFactory;
	private final BiFunction<ImmutableSession, C, SessionActivationNotifier> notifierFactory;
	private final Function<String, SessionAttributeActivationNotifier> detachedNotifierFactory;
	private final ListenerRegistration evictListenerRegistration;
	private final ListenerRegistration prePassivateListenerRegistration;
	private final ListenerRegistration postActivateListenerRegistration;

	public CoarseSessionAttributesFactory(SessionAttributesFactoryConfiguration<Map<String, Object>, V> configuration, BiFunction<ImmutableSession, C, SessionActivationNotifier> notifierFactory, Function<String, SessionAttributeActivationNotifier> detachedNotifierFactory, EmbeddedCacheConfiguration infinispan) {
		this.cache = infinispan.getCache();
		this.writeCache = infinispan.getWriteOnlyCache();
		this.silentCache = infinispan.getSilentWriteCache();
		this.marshaller = configuration.getMarshaller();
		this.immutability = configuration.getImmutability();
		this.properties = infinispan.getCacheProperties();
		this.mutatorFactory = new EmbeddedCacheMutatorFactory<>(this.cache, this.properties);
		this.notifierFactory = notifierFactory;
		this.detachedNotifierFactory = detachedNotifierFactory;
		this.prePassivateListenerRegistration = !this.properties.isPersistent() ? new PrePassivateBlockingListener<>(this.cache, this::prePassivate).register(SessionAttributesKey.class) : null;
		this.postActivateListenerRegistration = !this.properties.isPersistent() ? new PostActivateBlockingListener<>(this.cache, this::postActivate).register(SessionAttributesKey.class) : null;
		this.evictListenerRegistration = new PostPassivateBlockingListener<>(infinispan.getCache(), this::cascadeEvict).register(SessionMetaDataKey.class);
	}

	@Override
	public void close() {
		this.evictListenerRegistration.close();
		if (this.prePassivateListenerRegistration != null) {
			this.prePassivateListenerRegistration.close();
		}
		if (this.postActivateListenerRegistration != null) {
			this.postActivateListenerRegistration.close();
		}
	}

	@Override
	public CompletionStage<Map<String, Object>> createValueAsync(String id, Void context) {
		Map<String, Object> attributes = new ConcurrentHashMap<>();
		try {
			V value = this.marshaller.write(attributes);
			return this.writeCache.putAsync(new SessionAttributesKey(id), value).thenApply(v -> attributes);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public CompletionStage<Map<String, Object>> findValueAsync(String id) {
		return this.getValueAsync(id).exceptionally(e -> {
			LOGGER.warn(e.getLocalizedMessage(), e);
			this.removeAsync(id);
			return null;
		});
	}

	@Override
	public CompletionStage<Map<String, Object>> tryValueAsync(String id) {
		return this.getValueAsync(id).exceptionally(e -> null);
	}

	private CompletionStage<Map<String, Object>> getValueAsync(String id) {
		return this.cache.getAsync(new SessionAttributesKey(id)).thenApply(value -> {
			try {
				return (value != null) ? this.marshaller.read(value) : null;
			} catch (IOException e) {
				throw new UncheckedIOException(id, e);
			}
		});
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.deleteAsync(this.writeCache, id);
	}

	@Override
	public CompletionStage<Void> purgeAsync(String id) {
		return this.deleteAsync(this.silentCache, id);
	}

	public CompletionStage<Void> deleteAsync(Cache<SessionAttributesKey, V> cache, String id) {
		return cache.removeAsync(new SessionAttributesKey(id)).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public Map<String, Object> createImmutableSessionAttributes(String id, Map<String, Object> attributes) {
		return Map.copyOf(attributes);
	}

	@Override
	public SessionAttributes createSessionAttributes(String id, Map<String, Object> attributes, ImmutableSessionMetaData metaData, C context) {
		try {
			CacheEntryMutator mutator = (this.properties.isTransactional() && metaData.isNew()) ? CacheEntryMutator.NO_OP : this.mutatorFactory.createMutator(new SessionAttributesKey(id), this.marshaller.write(attributes));
			SessionActivationNotifier notifier = this.properties.isPersistent() ? this.notifierFactory.apply(new CompositeImmutableSession(id, metaData, this.createImmutableSessionAttributes(id, attributes)), context) : null;
			return new CoarseSessionAttributes(attributes, mutator, this.properties.isMarshalling() ? this.marshaller : Marshallability.TRUE , this.immutability, notifier);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void cascadeEvict(SessionMetaDataKey key) {
		this.cache.evict(new SessionAttributesKey(key.getId()));
	}

	private void prePassivate(SessionAttributesKey key, V value) {
		this.notify(key, value, SessionAttributeActivationNotifier.PRE_PASSIVATE);
	}

	private void postActivate(SessionAttributesKey key, V value) {
		this.notify(key, value, SessionAttributeActivationNotifier.POST_ACTIVATE);
	}

	private void notify(SessionAttributesKey key, V value, BiConsumer<SessionAttributeActivationNotifier, Object> notification) {
		String id = key.getId();
		try (SessionAttributeActivationNotifier notifier = this.detachedNotifierFactory.apply(id)) {
			Map<String, Object> attributes = this.marshaller.read(value);
			for (Object attributeValue : attributes.values()) {
				notification.accept(notifier, attributeValue);
			}
		} catch (IOException e) {
			LOGGER.warn(id, e);
		}
	}
}
