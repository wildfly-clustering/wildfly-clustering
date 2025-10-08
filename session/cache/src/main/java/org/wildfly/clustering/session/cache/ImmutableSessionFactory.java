/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiCacheEntryLocator;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.attributes.ImmutableSessionAttributesFactory;

/**
 * Factory for creating an {@link ImmutableSession}.
 * @param <MV> the session metadata value type
 * @param <AV> the session attribute value type
 * @author Paul Ferraro
 */
public interface ImmutableSessionFactory<MV, AV> extends ImmutableSessionFactoryConfiguration<MV, AV>, BiCacheEntryLocator<String, MV, AV> {

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> findEntry(String id) {
		CompletionStage<MV> metaDataStage = this.getSessionMetaDataFactory().findValueAsync(id);
		ImmutableSessionAttributesFactory<AV> attributesFactory = this.getSessionAttributesFactory();
		// If cache locks on read, find meta data first
		CompletionStage<AV> attributesStage = this.getCacheProperties().isLockOnRead() ? metaDataStage.thenCompose(metaData -> (metaData != null) ? attributesFactory.findValueAsync(id) : CompletableFuture.completedStage(null)) : attributesFactory.findValueAsync(id);
		return Map.entry(metaDataStage, attributesStage);
	}

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> tryEntry(String id) {
		CompletionStage<MV> metaDataStage = this.getSessionMetaDataFactory().tryValueAsync(id);
		ImmutableSessionAttributesFactory<AV> attributesFactory = this.getSessionAttributesFactory();
		// If cache locks on read, find meta data first
		CompletionStage<AV> attributesStage = this.getCacheProperties().isLockOnRead() ? metaDataStage.thenCompose(metaData -> (metaData != null) ? attributesFactory.findValueAsync(id) : CompletableFuture.completedStage(null)) : attributesFactory.tryValueAsync(id);
		return Map.entry(metaDataStage, attributesStage);
	}

	/**
	 * Creates an immutable session from the specified identifier and metadata/attribute entry.
	 * @param id a session identifier
	 * @param entry a map entry containing the metadata and attributes of the session
	 * @return an immutable session
	 */
	default ImmutableSession createImmutableSession(String id, Map.Entry<MV, AV> entry) {
		MV metaDataValue = entry.getKey();
		AV attributesValue = entry.getValue();
		if ((metaDataValue == null) || (attributesValue == null)) return null;
		ImmutableSessionMetaData metaData = this.getSessionMetaDataFactory().createImmutableSessionMetaData(id, metaDataValue);
		Map<String, Object> attributes = this.getSessionAttributesFactory().createImmutableSessionAttributes(id, attributesValue);
		return this.createImmutableSession(id, metaData, attributes);
	}

	/**
	 * Creates an immutable session from the specified identifier, metadata, and attributes.
	 * @param id the identifier of this session
	 * @param metaData the metadata of this session
	 * @param attributes the attributes of this session
	 * @return an immutable session
	 */
	ImmutableSession createImmutableSession(String id, ImmutableSessionMetaData metaData, Map<String, Object> attributes);
}
