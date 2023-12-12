/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiCreator;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;

/**
 * Factory for creating sessions. Encapsulates the cache mapping strategy for sessions.
 * @param <DC> the deployment context type
 * @param <MV> the meta-data value type
 * @param <AV> the attributes value type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public interface SessionFactory<DC, MV, AV, SC> extends ImmutableSessionFactory<MV, AV>, BiCreator<String, MV, AV, Duration>, Remover<String>, Registration {
	@Override
	SessionMetaDataFactory<MV> getMetaDataFactory();
	@Override
	SessionAttributesFactory<DC, AV> getAttributesFactory();

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> createEntry(String id, Duration context) {
		return Map.entry(this.getMetaDataFactory().createValueAsync(id, context), this.getAttributesFactory().createValueAsync(id, null));
	}

	@Override
	default CompletionStage<Void> removeAsync(String id) {
		return CompletableFuture.allOf(this.getMetaDataFactory().removeAsync(id).toCompletableFuture(), this.getAttributesFactory().removeAsync(id).toCompletableFuture());
	}

	@Override
	default CompletionStage<Void> purgeAsync(String id) {
		return CompletableFuture.allOf(this.getMetaDataFactory().purgeAsync(id).toCompletableFuture(), this.getAttributesFactory().purgeAsync(id).toCompletableFuture());
	}

	Session<SC> createSession(String id, Map.Entry<MV, AV> entry, DC context);

	@Override
	default void close() {
		this.getMetaDataFactory().close();
		this.getAttributesFactory().close();
	}
}
