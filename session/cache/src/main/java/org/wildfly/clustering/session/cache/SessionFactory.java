/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiCacheEntryCreator;
import org.wildfly.clustering.cache.CacheEntryRemover;
import org.wildfly.clustering.session.Session;

/**
 * Factory for creating sessions. Encapsulates the cache mapping strategy for sessions.
 * @param <DC> the deployment context type
 * @param <MV> the meta-data value type
 * @param <AV> the attributes value type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface SessionFactory<DC, MV, AV, SC> extends ImmutableSessionFactory<MV, AV>, SessionFactoryConfiguration<DC, MV, AV, SC>, BiCacheEntryCreator<String, MV, AV, Duration>, CacheEntryRemover<String>, AutoCloseable {

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> createEntry(String id, Duration context) {
		return Map.entry(this.getSessionMetaDataFactory().createValueAsync(id, context), this.getSessionAttributesFactory().createValueAsync(id, null));
	}

	@Override
	default CompletionStage<Void> removeAsync(String id) {
		return CompletableFuture.allOf(this.getSessionMetaDataFactory().removeAsync(id).toCompletableFuture(), this.getSessionAttributesFactory().removeAsync(id).toCompletableFuture());
	}

	@Override
	default CompletionStage<Void> purgeAsync(String id) {
		return CompletableFuture.allOf(this.getSessionMetaDataFactory().purgeAsync(id).toCompletableFuture(), this.getSessionAttributesFactory().purgeAsync(id).toCompletableFuture());
	}

	/**
	 * Creates a session from the specified identifier, metadata, attributes, and context.
	 * @param id a session identifier
	 * @param entry a map entry containing the metadata and attributes of the session
	 * @param context the session context
	 * @return a session from the specified identifier, metadata, attributes, and context.
	 */
	Session<SC> createSession(String id, Map.Entry<MV, AV> entry, DC context);

	@Override
	default void close() {
		this.getSessionMetaDataFactory().close();
		this.getSessionAttributesFactory().close();
	}
}
