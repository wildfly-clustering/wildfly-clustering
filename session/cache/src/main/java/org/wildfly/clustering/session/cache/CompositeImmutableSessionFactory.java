/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.attributes.ImmutableSessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * An immutable session factory that delegates to immutable factories for metadata and attributes.
 * @author Paul Ferraro
 * @param <MV> the session metadata type
 * @param <AV> the session attributes type
 */
public class CompositeImmutableSessionFactory<MV, AV> implements ImmutableSessionFactory<MV, AV> {
	private final ImmutableSessionMetaDataFactory<MV> metaDataFactory;
	private final ImmutableSessionAttributesFactory<AV> attributesFactory;
	private final CacheProperties properties;

	/**
	 * Creates an immutable session factory.
	 * @param metaDataFactory a metadata factory
	 * @param attributesFactory an attributes factory
	 * @param properties the properties of the associated cache
	 */
	public CompositeImmutableSessionFactory(ImmutableSessionMetaDataFactory<MV> metaDataFactory, ImmutableSessionAttributesFactory<AV> attributesFactory, CacheProperties properties) {
		this.metaDataFactory = metaDataFactory;
		this.attributesFactory = attributesFactory;
		this.properties = properties;
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

	@Override
	public CacheProperties getCacheProperties() {
		return this.properties;
	}
}
