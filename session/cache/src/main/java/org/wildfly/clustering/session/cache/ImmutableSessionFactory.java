/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.BiLocator;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.attributes.ImmutableSessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * Factory for creating an {@link ImmutableSession}.
 * @author Paul Ferraro
 */
public interface ImmutableSessionFactory<MV, AV> extends BiLocator<String, MV, AV> {

	ImmutableSessionMetaDataFactory<MV> getMetaDataFactory();
	ImmutableSessionAttributesFactory<AV> getAttributesFactory();

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> findEntry(String id) {
		return Map.entry(this.getMetaDataFactory().findValueAsync(id), this.getAttributesFactory().findValueAsync(id));
	}

	@Override
	default Map.Entry<CompletionStage<MV>, CompletionStage<AV>> tryEntry(String id) {
		return Map.entry(this.getMetaDataFactory().tryValueAsync(id), this.getAttributesFactory().tryValueAsync(id));
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
