/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;

/**
 * Configuration for a factory for creating {@link SessionAttributes} objects, based on marshalled values.
 * @author Paul Ferraro
 * @param <V> the attributes value type
 * @param <MV> the serialized attributes value type
 */
public abstract class MarshallerSessionAttributesFactoryConfiguration<V, MV> implements SessionAttributesFactoryConfiguration<V, MV> {
	private final Immutability immutability;
	private final Marshaller<V, MV> marshaller;

	protected <SC> MarshallerSessionAttributesFactoryConfiguration(SessionManagerFactoryConfiguration<SC> configuration, Marshaller<V, MV> marshaller) {
		this.immutability = configuration.getImmutability();
		this.marshaller = marshaller;
	}

	@Override
	public Marshaller<V, MV> getMarshaller() {
		return this.marshaller;
	}

	@Override
	public Immutability getImmutability() {
		return this.immutability;
	}
}
