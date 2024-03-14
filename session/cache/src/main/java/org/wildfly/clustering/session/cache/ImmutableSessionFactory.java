/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiLocator;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.attributes.ImmutableSessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * Factory for creating an {@link ImmutableSession}.
 * @param <MV> the session metadata value type
 * @param <AV> the session attribute value type
 * @author Paul Ferraro
 */
public interface ImmutableSessionFactory<MV, AV> extends BiLocator<String, MV, AV> {

	ImmutableSessionMetaDataFactory<MV> getMetaDataFactory();
	ImmutableSessionAttributesFactory<AV> getAttributesFactory();
	CacheProperties getCacheProperties();

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> findEntry(String id) {
		CompletionStage<MV> metaDataStage = this.getMetaDataFactory().findValueAsync(id);
		// If cache locks on read, find meta data first
		CompletionStage<AV> attributesStage = this.getCacheProperties().isLockOnRead() ? metaDataStage.thenCompose(metaData -> (metaData != null) ? this.getAttributesFactory().findValueAsync(id) : CompletableFuture.completedStage(null)) : this.getAttributesFactory().findValueAsync(id);
		return Map.entry(metaDataStage, attributesStage);
	}

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> tryEntry(String id) {
		CompletionStage<MV> metaDataStage = this.getMetaDataFactory().tryValueAsync(id);
		// If cache locks on read, find meta data first
		CompletionStage<AV> attributesStage = this.getCacheProperties().isLockOnRead() ? metaDataStage.thenCompose(metaData -> (metaData != null) ? this.getAttributesFactory().findValueAsync(id) : CompletableFuture.completedStage(null)) : this.getAttributesFactory().tryValueAsync(id);
		return Map.entry(metaDataStage, attributesStage);
	}

	default ImmutableSession createImmutableSession(String id, Map.Entry<MV, AV> entry) {
		MV metaDataValue = entry.getKey();
		AV attributesValue = entry.getValue();
		if ((metaDataValue == null) || (attributesValue == null)) return null;
		ImmutableSessionMetaData metaData = this.getMetaDataFactory().createImmutableSessionMetaData(id, metaDataValue);
		Map<String, Object> attributes = this.getAttributesFactory().createImmutableSessionAttributes(id, attributesValue);
		return this.createImmutableSession(id, metaData, attributes);
	}

	ImmutableSession createImmutableSession(String id, ImmutableSessionMetaData metaData, Map<String, Object> attributes);
}
