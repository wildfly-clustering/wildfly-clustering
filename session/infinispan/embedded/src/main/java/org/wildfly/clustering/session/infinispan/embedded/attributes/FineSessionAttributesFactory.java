/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.attributes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.infinispan.Cache;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.embedded.listener.ListenerRegistration;
import org.wildfly.clustering.cache.infinispan.embedded.listener.PostActivateBlockingListener;
import org.wildfly.clustering.cache.infinispan.embedded.listener.PostPassivateBlockingListener;
import org.wildfly.clustering.cache.infinispan.embedded.listener.PrePassivateBlockingListener;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeImmutableSession;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.fine.FineSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeMapComputeFunction;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;

/**
 * {@link SessionAttributesFactory} for fine granularity sessions, where all session attributes are stored in a single cache entry,
 * but changes are applied by functions such that only modified and mutated values ever replicate/persist.
 * This implementation is unsuited for attributes that strongly reference each other.
 * @param <C> the session manager context type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class FineSessionAttributesFactory<C, V> implements SessionAttributesFactory<C, Map<String, Object>> {
	private static final System.Logger LOGGER = System.getLogger(FineSessionAttributesFactory.class.getName());

	private final Cache<SessionAttributesKey, Map<String, V>> cache;
	private final Cache<SessionAttributesKey, Map<String, V>> writeCache;
	private final Cache<SessionAttributesKey, Map<String, V>> silentCache;
	private final Marshaller<Object, V> marshaller;
	private final Predicate<Object> immutability;
	private final CacheProperties properties;
	private final CacheEntryMutatorFactory<SessionAttributesKey, Map<String, V>> mutatorFactory;
	private final BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> notifierFactory;
	private final Function<String, SessionAttributeActivationNotifier> detachedNotifierFactory;
	private final ListenerRegistration evictListenerRegistration;
	private final ListenerRegistration prePassivateListenerRegistration;
	private final ListenerRegistration postActivateListenerRegistration;

	/**
	 * Creates a factory for fine-granularity session attributes entry.
	 * @param configuration the configuration for this factory
	 * @param notifierFactory the notifier factory
	 * @param detachedNotifierFactory the detached notifier factory
	 * @param infinispan the configuration of the associated cache
	 */
	public FineSessionAttributesFactory(SessionAttributesFactoryConfiguration<Object, V> configuration, BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> notifierFactory, Function<String, SessionAttributeActivationNotifier> detachedNotifierFactory, EmbeddedCacheConfiguration infinispan) {
		this.cache = infinispan.getCache();
		this.writeCache = infinispan.getWriteOnlyCache();
		this.silentCache = infinispan.getSilentWriteCache();
		this.marshaller = configuration.getMarshaller();
		this.immutability = configuration.getImmutability();
		this.properties = infinispan.getCacheProperties();
		this.mutatorFactory = infinispan.getCacheEntryMutatorFactory(SessionAttributeMapComputeFunction::new);
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
		return CompletableFuture.completedStage(this.createValue(id, context));
	}

	@Override
	public Map<String, Object> createValue(String id, Void context) {
		return new ConcurrentHashMap<>();
	}

	@Override
	public CompletionStage<Map<String, Object>> findValueAsync(String id) {
		return this.getValueAsync(id).exceptionally(e -> {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			this.removeAsync(id);
			return null;
		});
	}

	@Override
	public CompletionStage<Map<String, Object>> tryValueAsync(String id) {
		return this.getValueAsync(id).exceptionally(Function.empty());
	}

	private CompletionStage<Map<String, Object>> getValueAsync(String id) {
		return this.cache.getAsync(new SessionAttributesKey(id)).thenApply(value -> {
			Map<String, Object> attributes = this.createValue(id, null);
			if (value != null) {
				for (Map.Entry<String, V> entry : value.entrySet()) {
					String attributeName = entry.getKey();
					try {
						attributes.put(attributeName, this.marshaller.read(entry.getValue()));
					} catch (IOException e) {
						throw new UncheckedIOException(attributeName, e);
					}
				}
			}
			return attributes;
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

	private CompletionStage<Void> deleteAsync(Cache<SessionAttributesKey, Map<String, V>> cache, String id) {
		return cache.removeAsync(new SessionAttributesKey(id)).thenAccept(Consumer.empty());
	}

	@Override
	public Map<String, Object> createImmutableSessionAttributes(String id, Map<String, Object> attributes) {
		return Map.copyOf(attributes);
	}

	@Override
	public SessionAttributes createSessionAttributes(String id, Map<String, Object> attributes, ImmutableSessionMetaData metaData, C context) {
		SessionAttributeActivationNotifier notifier = this.properties.isPersistent() ? this.notifierFactory.apply(new CompositeImmutableSession(id, metaData, attributes), context) : null;
		return new FineSessionAttributes<>(new SessionAttributesKey(id), attributes, this.mutatorFactory, this.marshaller, this.immutability, this.properties, notifier);
	}

	private void cascadeEvict(SessionMetaDataKey key) {
		this.cache.evict(new SessionAttributesKey(key.getId()));
	}

	private void prePassivate(SessionAttributesKey key, Map<String, V> attributes) {
		this.notify(SessionAttributeActivationNotifier::prePassivate, key, attributes);
	}

	private void postActivate(SessionAttributesKey key, Map<String, V> attributes) {
		this.notify(SessionAttributeActivationNotifier::postActivate, key, attributes);
	}

	private void notify(BiConsumer<SessionAttributeActivationNotifier, Object> notification, SessionAttributesKey key, Map<String, V> attributes) {
		String id = key.getId();
		SessionAttributeActivationNotifier notifier = this.detachedNotifierFactory.apply(id);
		for (Map.Entry<String, V> entry : attributes.entrySet()) {
			try {
				notification.accept(notifier, this.marshaller.read(entry.getValue()));
			} catch (IOException e) {
				LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
	}
}
