/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.attributes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheComputeMutatorFactory;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.ImmutableSessionAttributes;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeImmutableSession;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SimpleImmutableSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.fine.FineSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.fine.ImmutableSessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeMapComputeFunction;
import org.wildfly.clustering.session.container.SessionActivationListenerFacadeProvider;
import org.wildfly.common.function.Functions;

/**
 * {@link SessionAttributesFactory} for fine granularity sessions.
 * A given session's attributes are mapped to N+1 co-located cache entries, where N is the number of session attributes.
 * A separate cache entry stores the activate attribute names for the session.
 * @author Paul Ferraro
 */
public class FineSessionAttributesFactory<S, C, L, V> implements SessionAttributesFactory<C, Map<String, Object>> {
	private static final Logger LOGGER = Logger.getLogger(FineSessionAttributesFactory.class);

	private final RemoteCache<SessionAttributesKey, Map<String, V>> cache;
	private final Flag[] ignoreReturnFlags;
	private final Marshaller<Object, V> marshaller;
	private final Immutability immutability;
	private final CacheProperties properties;
	private final CacheEntryMutatorFactory<SessionAttributesKey, Map<String, V>> mutatorFactory;
	private final SessionActivationListenerFacadeProvider<S, C, L> provider;

	public FineSessionAttributesFactory(SessionAttributesFactoryConfiguration<S, C, L, Object, V> configuration, RemoteCacheConfiguration hotrod) {
		this.cache = hotrod.getCache();
		this.ignoreReturnFlags = hotrod.getIgnoreReturnFlags();
		this.marshaller = configuration.getMarshaller();
		this.immutability = configuration.getImmutability();
		this.properties = hotrod.getCacheProperties();
		this.mutatorFactory = new RemoteCacheComputeMutatorFactory<>(this.cache, this.ignoreReturnFlags, SessionAttributeMapComputeFunction::new);
		this.provider = configuration.getSessionActivationListenerProvider();
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
		return this.getValueAsync(id).exceptionally(e -> null);
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
		return this.cache.withFlags(this.ignoreReturnFlags).removeAsync(new SessionAttributesKey(id)).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public SessionAttributes createSessionAttributes(String id, Map<String, Object> attributes, ImmutableSessionMetaData metaData, C context) {
		SessionAttributeActivationNotifier notifier = this.properties.isPersistent() ? new ImmutableSessionAttributeActivationNotifier<>(this.provider, new CompositeImmutableSession(id, metaData, this.createImmutableSessionAttributes(id, attributes)), context) : null;
		return new FineSessionAttributes<>(new SessionAttributesKey(id), attributes, this.mutatorFactory, this.marshaller, this.immutability, this.properties, notifier);
	}

	@Override
	public ImmutableSessionAttributes createImmutableSessionAttributes(String id, Map<String, Object> attributes) {
		return new SimpleImmutableSessionAttributes(attributes);
	}

	@Override
	public void close() {
	}
}
