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

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;
import org.wildfly.clustering.cache.CacheEntryMutator;
import org.wildfly.clustering.cache.CacheEntryMutatorFactory;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheMutatorFactory;
import org.wildfly.clustering.marshalling.Marshallability;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.ImmutableSessionAttributes;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.CompositeImmutableSession;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.SimpleImmutableSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.coarse.CoarseSessionAttributes;
import org.wildfly.clustering.session.cache.attributes.coarse.ImmutableSessionActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.coarse.SessionActivationNotifier;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class CoarseSessionAttributesFactory<S, C, L, V> implements SessionAttributesFactory<C, Map<String, Object>> {
	private static final Logger LOGGER = Logger.getLogger(CoarseSessionAttributesFactory.class);

	private final RemoteCache<SessionAttributesKey, V> cache;
	private final Flag[] ignoreReturnFlags;
	private final Marshaller<Map<String, Object>, V> marshaller;
	private final Immutability immutability;
	private final CacheProperties properties;
	private final CacheEntryMutatorFactory<SessionAttributesKey, V> mutatorFactory;
	private final SessionSpecificationProvider<S, C, L> provider;

	public CoarseSessionAttributesFactory(SessionAttributesFactoryConfiguration<Map<String, Object>, V> configuration, SessionSpecificationProvider<S, C, L> provider, RemoteCacheConfiguration hotrod) {
		this.cache = hotrod.getCache();
		this.ignoreReturnFlags = hotrod.getIgnoreReturnFlags();
		this.marshaller = configuration.getMarshaller();
		this.immutability = configuration.getImmutability();
		this.properties = hotrod.getCacheProperties();
		this.mutatorFactory = new RemoteCacheMutatorFactory<>(this.cache, this.ignoreReturnFlags);
		this.provider = provider;
	}

	@Override
	public CompletionStage<Map<String, Object>> createValueAsync(String id, Void context) {
		Map<String, Object> attributes = new ConcurrentHashMap<>();
		try {
			V value = this.marshaller.write(attributes);
			return this.cache.withFlags(this.ignoreReturnFlags).putAsync(new SessionAttributesKey(id), value).thenApply(v -> attributes);
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
		return this.cache.withFlags(this.ignoreReturnFlags).removeAsync(new SessionAttributesKey(id)).thenAccept(Functions.discardingConsumer());
	}

	@Override
	public SessionAttributes createSessionAttributes(String id, Map<String, Object> attributes, ImmutableSessionMetaData metaData, C context) {
		try {
			CacheEntryMutator mutator = this.mutatorFactory.createMutator(new SessionAttributesKey(id), this.marshaller.write(attributes));
			SessionActivationNotifier notifier = this.properties.isPersistent() ? new ImmutableSessionActivationNotifier<>(this.provider, new CompositeImmutableSession(id, metaData, this.createImmutableSessionAttributes(id, attributes)), context) : null;
			return new CoarseSessionAttributes(attributes, mutator, this.properties.isMarshalling() ? this.marshaller : Marshallability.TRUE, this.immutability, notifier);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ImmutableSessionAttributes createImmutableSessionAttributes(String id, Map<String, Object> values) {
		return new SimpleImmutableSessionAttributes(values);
	}

	@Override
	public void close() {
	}
}
