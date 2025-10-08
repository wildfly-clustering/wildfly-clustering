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
	 * Creates an immutable session factory from the specified configuration.
	 * @param configuration a session factory configuration
	 */
	public CompositeImmutableSessionFactory(ImmutableSessionFactoryConfiguration<MV, AV> configuration) {
		this.metaDataFactory = configuration.getSessionMetaDataFactory();
		this.attributesFactory = configuration.getSessionAttributesFactory();
		this.properties = configuration.getCacheProperties();
	}

	@Override
	public ImmutableSessionMetaDataFactory<MV> getSessionMetaDataFactory() {
		return this.metaDataFactory;
	}

	@Override
	public ImmutableSessionAttributesFactory<AV> getSessionAttributesFactory() {
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
