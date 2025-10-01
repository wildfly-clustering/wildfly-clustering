/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;

/**
 * A session factory composed of metadata, attribute, and context factories.
 * @param <C> the session manager context type
 * @param <MV> the session metadata type
 * @param <AV> the session attributes type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class CompositeSessionFactory<C, MV extends Contextual<SC>, AV, SC> extends CompositeImmutableSessionFactory<MV, AV> implements SessionFactory<C, MV, AV, SC> {
	private final SessionMetaDataFactory<MV> metaDataFactory;
	private final SessionAttributesFactory<C, AV> attributesFactory;
	private final Supplier<SC> contextFactory;

	/**
	 * Creates a session factory composed from metadata, attribute, and context factories.
	 * @param metaDataFactory the session metadata factory
	 * @param attributesFactory the session atttributes factory
	 * @param properties the cache properties
	 * @param contextFactory the session context factory
	 */
	public CompositeSessionFactory(SessionMetaDataFactory<MV> metaDataFactory, SessionAttributesFactory<C, AV> attributesFactory, CacheProperties properties, Supplier<SC> contextFactory) {
		super(metaDataFactory, attributesFactory, properties);
		this.metaDataFactory = metaDataFactory;
		this.attributesFactory = attributesFactory;
		this.contextFactory = contextFactory;
	}

	@Override
	public SessionMetaDataFactory<MV> getMetaDataFactory() {
		return this.metaDataFactory;
	}

	@Override
	public SessionAttributesFactory<C, AV> getAttributesFactory() {
		return this.attributesFactory;
	}

	@Override
	public Supplier<SC> getContextFactory() {
		return this.contextFactory;
	}

	@Override
	public Session<SC> createSession(String id, Map.Entry<MV, AV> entry, C context) {
		MV metaDataValue = entry.getKey();
		AV attributesValue = entry.getValue();
		if ((metaDataValue == null) || (attributesValue == null)) return null;
		InvalidatableSessionMetaData metaData = this.metaDataFactory.createSessionMetaData(id, metaDataValue);
		SessionAttributes attributes = this.attributesFactory.createSessionAttributes(id, attributesValue, metaData, context);
		return new CompositeSession<>(id, metaData, attributes, metaDataValue.getContext(), this.contextFactory, this);
	}
}
