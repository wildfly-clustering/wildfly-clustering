/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.attributes;

import static org.wildfly.clustering.cache.function.Functions.nullFunction;
import static org.wildfly.common.function.Functions.discardingConsumer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheEntryComputerFactory;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeImmutableSession;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.fine.FineSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeMapComputeFunction;

/**
 * {@link SessionAttributesFactory} for fine granularity sessions, where all session attributes are stored in a single cache entry,
 * but changes are applied by functions such that only modified and mutated values ever replicate/persist.
 * This implementation is unsuited for attributes that strongly reference each other.
 * @param <C> the session manager context type
 * @param <V> the cache value type
 * @author Paul Ferraro
 */
public class FineSessionAttributesFactory<C, V> implements SessionAttributesFactory<C, Map<String, Object>> {
	private static final Logger LOGGER = Logger.getLogger(FineSessionAttributesFactory.class);

	private final RemoteCache<SessionAttributesKey, Map<String, V>> cache;
	private final Flag[] ignoreReturnFlags;
	private final Marshaller<Object, V> marshaller;
	private final Immutability immutability;
	private final CacheProperties properties;
	private final CacheEntryMutatorFactory<SessionAttributesKey, Map<String, V>> mutatorFactory;
	private final BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> notifierFactory;

	public FineSessionAttributesFactory(SessionAttributesFactoryConfiguration<Object, V> configuration, BiFunction<ImmutableSession, C, SessionAttributeActivationNotifier> notifierFactory, RemoteCacheConfiguration hotrod) {
		this.cache = hotrod.getCache();
		this.ignoreReturnFlags = hotrod.getIgnoreReturnFlags();
		this.marshaller = configuration.getMarshaller();
		this.immutability = configuration.getImmutability();
		this.properties = hotrod.getCacheProperties();
		this.mutatorFactory = new RemoteCacheEntryComputerFactory<>(this.cache, this.ignoreReturnFlags, SessionAttributeMapComputeFunction::new);
		this.notifierFactory = notifierFactory;
	}

	@Override
	public Map<String, Object> createValue(String id, Void context) {
		return new ConcurrentHashMap<>();
	}

	@Override
	public CompletionStage<Map<String, Object>> createValueAsync(String id, Void context) {
		return CompletableFuture.completedStage(this.createValue(id, context));
	}

	@Override
	public CompletionStage<Map<String, Object>> findValueAsync(String id) {
		return this.getValueAsync(id).exceptionally(e -> {
			LOGGER.warn(e.getLocalizedMessage(), e);
			this.purgeAsync(id);
			return null;
		});
	}

	@Override
	public CompletionStage<Map<String, Object>> tryValueAsync(String id) {
		return this.getValueAsync(id).exceptionally(nullFunction());
	}

	private CompletionStage<Map<String, Object>> getValueAsync(String id) {
		return this.cache.getAsync(new SessionAttributesKey(id)).thenApply(values -> {
			Map<String, Object> attributes = this.createValue(id, null);
			if (values != null) {
				for (Map.Entry<String, V> entry : values.entrySet()) {
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
		return this.cache.withFlags(this.ignoreReturnFlags).removeAsync(new SessionAttributesKey(id)).thenAccept(discardingConsumer());
	}

	@Override
	public SessionAttributes createSessionAttributes(String id, Map<String, Object> attributes, ImmutableSessionMetaData metaData, C context) {
		SessionAttributeActivationNotifier notifier = this.properties.isPersistent() ? this.notifierFactory.apply(new CompositeImmutableSession(id, metaData, this.createImmutableSessionAttributes(id, attributes)), context) : null;
		return new FineSessionAttributes<>(new SessionAttributesKey(id), attributes, this.mutatorFactory, this.marshaller, this.immutability, this.properties, notifier);
	}

	@Override
	public Map<String, Object> createImmutableSessionAttributes(String id, Map<String, Object> attributes) {
		return Map.copyOf(attributes);
	}

	@Override
	public void close() {
	}
}
