/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.marshalling.ByteBufferMarshalledValueFactory;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.marshalling.MarshalledValueMarshaller;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;

/**
 * Configuration for a factory for creating {@link SessionAttributes} objects, based on marshalled values.
 * @author Paul Ferraro
 * @param <V> the attributes value type
 */
public class MarshalledValueMarshallerSessionAttributesFactoryConfiguration<V> extends AbstractSessionAttributesFactoryConfiguration<V, MarshalledValue<V, ByteBufferMarshaller>> {
	/**
	 * Creates a session attributes factory configuration
	 * @param <SC> a session context type
	 * @param configuration a session manager factory configuration
	 */
	public <SC> MarshalledValueMarshallerSessionAttributesFactoryConfiguration(SessionManagerFactoryConfiguration<SC> configuration) {
		super(configuration, new MarshalledValueMarshaller<>(new ByteBufferMarshalledValueFactory(configuration.getMarshaller())));
	}
}
