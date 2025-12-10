/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;

/**
 * A session factory composed of metadata, attribute, and context factories.
 * @param <CC> the deployment context type
 * @param <MV> the session metadata type
 * @param <AV> the session attributes type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class CompositeSessionFactory<CC, MV extends Contextual<SC>, AV, SC> extends CompositeImmutableSessionFactory<MV, AV> implements SessionFactory<CC, MV, AV, SC> {

	private final SessionMetaDataFactory<MV> metaDataFactory;
	private final SessionAttributesFactory<CC, AV> attributesFactory;
	private final Supplier<SC> contextFactory;

	/**
	 * Creates a session factory composed from metadata, attribute, and context factories.
	 * @param configuration the configuration of this session factory
	 */
	public CompositeSessionFactory(SessionFactoryConfiguration<CC, MV, AV, SC> configuration) {
		super(configuration);
		this.metaDataFactory = configuration.getSessionMetaDataFactory();
		this.attributesFactory = configuration.getSessionAttributesFactory();
		this.contextFactory = configuration.getSessionContextFactory();
	}

	@Override
	public SessionMetaDataFactory<MV> getSessionMetaDataFactory() {
		return this.metaDataFactory;
	}

	@Override
	public SessionAttributesFactory<CC, AV> getSessionAttributesFactory() {
		return this.attributesFactory;
	}

	@Override
	public Supplier<SC> getSessionContextFactory() {
		return this.contextFactory;
	}

	@Override
	public Session<SC> createSession(String id, Map.Entry<MV, AV> entry, CC context) {
		MV metaDataValue = entry.getKey();
		AV attributesValue = entry.getValue();
		if ((metaDataValue == null) || (attributesValue == null)) return null;
		InvalidatableSessionMetaData metaData = this.metaDataFactory.createSessionMetaData(id, metaDataValue);
		SessionAttributes attributes = this.attributesFactory.createSessionAttributes(id, attributesValue, metaData, context);
		return new CompositeSession<>(id, metaData, attributes, metaDataValue.getContext(), this.contextFactory, this);
	}
}
