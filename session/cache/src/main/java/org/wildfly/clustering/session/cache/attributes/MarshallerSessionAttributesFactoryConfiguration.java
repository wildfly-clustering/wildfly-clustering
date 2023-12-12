/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.container.SessionActivationListenerFacadeProvider;

/**
 * Configuration for a factory for creating {@link SessionAttributes} objects, based on marshalled values.
 * @author Paul Ferraro
 * @param <S> the HttpSession specification type
 * @param <DC> the ServletContext specification type
 * @param <L> the HttpSessionAttributeListener specification type
 * @param <V> the attributes value type
 * @param <MV> the serialized attributes value type
 */
public abstract class MarshallerSessionAttributesFactoryConfiguration<S, DC, L, V, MV> implements SessionAttributesFactoryConfiguration<S, DC, L, V, MV> {
	private final Immutability immutability;
	private final Marshaller<V, MV> marshaller;
	private final SessionActivationListenerFacadeProvider<S, DC, L> provider;

	protected <SC, B extends Batch> MarshallerSessionAttributesFactoryConfiguration(SessionManagerFactoryConfiguration<S, DC, L, SC, B> configuration, Marshaller<V, MV> marshaller) {
		this.immutability = configuration.getImmutability();
		this.marshaller = marshaller;
		this.provider = configuration.getContainerFacadeProvider();
	}

	@Override
	public Marshaller<V, MV> getMarshaller() {
		return this.marshaller;
	}

	@Override
	public Immutability getImmutability() {
		return this.immutability;
	}

	@Override
	public SessionActivationListenerFacadeProvider<S, DC, L> getSessionActivationListenerProvider() {
		return this.provider;
	}
}
