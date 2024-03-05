/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.attributes.ImmutableSessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * Generic immutable session factory implementation - independent of cache mapping strategy.
 * @author Paul Ferraro
 * @param <MV> the session metadata type
 * @param <AV> the session attributes type
 */
public class CompositeImmutableSessionFactory<MV, AV> implements ImmutableSessionFactory<MV, AV> {
	private final ImmutableSessionMetaDataFactory<MV> metaDataFactory;
	private final ImmutableSessionAttributesFactory<AV> attributesFactory;

	public CompositeImmutableSessionFactory(ImmutableSessionMetaDataFactory<MV> metaDataFactory, ImmutableSessionAttributesFactory<AV> attributesFactory) {
		this.metaDataFactory = metaDataFactory;
		this.attributesFactory = attributesFactory;
	}

	@Override
	public ImmutableSessionMetaDataFactory<MV> getMetaDataFactory() {
		return this.metaDataFactory;
	}

	@Override
	public ImmutableSessionAttributesFactory<AV> getAttributesFactory() {
		return this.attributesFactory;
	}

	@Override
	public ImmutableSession createImmutableSession(String id, ImmutableSessionMetaData metaData, Map<String, Object> attributes) {
		return new CompositeImmutableSession(id, metaData, attributes);
	}
}
