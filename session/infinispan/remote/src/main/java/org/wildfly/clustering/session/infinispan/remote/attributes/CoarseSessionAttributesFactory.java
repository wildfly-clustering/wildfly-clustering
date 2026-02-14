/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.attributes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeImmutableSession;
import org.wildfly.clustering.session.cache.attributes.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.coarse.CoarseSessionAttributes;

/**
 * {@link SessionAttributesFactory} for coarse granularity sessions, where all session attributes are stored in a single cache entry.
 * This implementation is safe for attributes that strongly reference each other.
 * @param <C> the session manager context type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class CoarseSessionAttributesFactory<C, V> implements SessionAttributesFactory<C, Map<String, Object>> {
	private static final System.Logger LOGGER = System.getLogger(CoarseSessionAttributesFactory.class.getName());

	private final RemoteCache<SessionAttributesKey, V> readCache;
	private final RemoteCache<SessionAttributesKey, V> writeCache;
	private final Marshaller<Map<String, Object>, V> marshaller;
	private final Immutability immutability;
	private final CacheProperties properties;
	private final CacheEntryMutatorFactory<SessionAttributesKey, V> mutatorFactory;
	private final BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> notifierFactory;

	/**
	 * Creates a session attributes factory.
	 * @param configuration the configuration of this factory
	 * @param notifierFactory a passivation/activation notifier factory
	 * @param hotrod the configuration of the associated cache
	 */
	public CoarseSessionAttributesFactory(SessionAttributesFactoryConfiguration<Map<String, Object>, V> configuration, BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> notifierFactory, RemoteCacheConfiguration hotrod) {
		this.readCache = hotrod.getCache();
		this.writeCache = hotrod.getIgnoreReturnCache();
		this.marshaller = configuration.getMarshaller();
		this.immutability = configuration.getImmutability();
		this.properties = hotrod.getCacheProperties();
		this.mutatorFactory = hotrod.getCacheEntryMutatorFactory();
		this.notifierFactory = notifierFactory;
	}

	@Override
	public CompletionStage<Map<String, Object>> createValueAsync(String id, Void context) {
		Map<String, Object> attributes = new ConcurrentHashMap<>();
		try {
			V value = this.marshaller.write(attributes);
			return this.writeCache.putAsync(new SessionAttributesKey(id), value).thenApply(Function.of(attributes));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
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
		return this.getValueAsync(id).exceptionally(Function.of(null));
	}

	private CompletionStage<Map<String, Object>> getValueAsync(String id) {
		return this.readCache.getAsync(new SessionAttributesKey(id)).thenApply(value -> {
			try {
				return (value != null) ? this.marshaller.read(value) : null;
			} catch (IOException e) {
				throw new UncheckedIOException(id, e);
			}
		});
	}

	@Override
	public CompletionStage<Void> removeAsync(String id) {
		return this.writeCache.removeAsync(new SessionAttributesKey(id)).thenAccept(Consumer.of());
	}

	@Override
	public SessionAttributes createSessionAttributes(String id, Map<String, Object> attributes, ImmutableSessionMetaData metaData, C context) {
		try {
			Runnable mutator = this.mutatorFactory.createMutator(new SessionAttributesKey(id), this.marshaller.write(attributes));
			SessionAttributeActivationNotifier notifier = this.properties.isPersistent() ? this.notifierFactory.apply(new CompositeImmutableSession(id, metaData, attributes), context) : SessionAttributeActivationNotifier.SILENT;
			return new CoarseSessionAttributes(attributes, mutator, this.properties.isMarshalling() ? this.marshaller : Predicate.of(true), this.immutability, notifier);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Map<String, Object> createImmutableSessionAttributes(String id, Map<String, Object> values) {
		return Map.copyOf(values);
	}

	@Override
	public void close() {
	}
}
