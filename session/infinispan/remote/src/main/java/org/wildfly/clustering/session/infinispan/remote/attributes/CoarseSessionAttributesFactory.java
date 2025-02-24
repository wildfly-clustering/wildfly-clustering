/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.attributes;

import static org.wildfly.clustering.cache.function.Functions.constantFunction;
import static org.wildfly.clustering.cache.function.Functions.nullFunction;
import static org.wildfly.common.function.Functions.discardingConsumer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheEntryMutatorFactory;
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

/**
 * {@link SessionAttributesFactory} for coarse granularity sessions, where all session attributes are stored in a single cache entry.
 * This implementation is safe for attributes that strongly reference each other.
 * @param <C> the session manager context type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class CoarseSessionAttributesFactory<C, V> implements SessionAttributesFactory<C, Map<String, Object>> {
	private static final Logger LOGGER = Logger.getLogger(CoarseSessionAttributesFactory.class);

	private final RemoteCache<SessionAttributesKey, V> readCache;
	private final RemoteCache<SessionAttributesKey, V> writeCache;
	private final Marshaller<Map<String, Object>, V> marshaller;
	private final Immutability immutability;
	private final CacheProperties properties;
	private final CacheEntryMutatorFactory<SessionAttributesKey, V> mutatorFactory;
	private final BiFunction<ImmutableSession, C, SessionActivationNotifier> notifierFactory;

	public CoarseSessionAttributesFactory(SessionAttributesFactoryConfiguration<Map<String, Object>, V> configuration, BiFunction<ImmutableSession, C, SessionActivationNotifier> notifierFactory, RemoteCacheConfiguration hotrod) {
		this.readCache = hotrod.getCache();
		this.writeCache = hotrod.getIgnoreReturnCache();
		this.marshaller = configuration.getMarshaller();
		this.immutability = configuration.getImmutability();
		this.properties = hotrod.getCacheProperties();
		this.mutatorFactory = new RemoteCacheEntryMutatorFactory<>(this.writeCache);
		this.notifierFactory = notifierFactory;
	}

	@Override
	public CompletionStage<Map<String, Object>> createValueAsync(String id, Void context) {
		Map<String, Object> attributes = new ConcurrentHashMap<>();
		try {
			V value = this.marshaller.write(attributes);
			return this.writeCache.putAsync(new SessionAttributesKey(id), value).thenApply(constantFunction(attributes));
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
		return this.getValueAsync(id).exceptionally(nullFunction());
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
		return this.writeCache.removeAsync(new SessionAttributesKey(id)).thenAccept(discardingConsumer());
	}

	@Override
	public SessionAttributes createSessionAttributes(String id, Map<String, Object> attributes, ImmutableSessionMetaData metaData, C context) {
		try {
			CacheEntryMutator mutator = this.mutatorFactory.createMutator(new SessionAttributesKey(id), this.marshaller.write(attributes));
			SessionActivationNotifier notifier = this.properties.isPersistent() ? this.notifierFactory.apply(new CompositeImmutableSession(id, metaData, attributes), context) : null;
			return new CoarseSessionAttributes(attributes, mutator, this.properties.isMarshalling() ? this.marshaller : Marshallability.TRUE, this.immutability, notifier);
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
