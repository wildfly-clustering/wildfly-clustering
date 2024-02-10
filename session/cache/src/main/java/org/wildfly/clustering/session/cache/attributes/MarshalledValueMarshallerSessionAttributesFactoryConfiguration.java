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
 * @param <S> the HttpSession specification type
 * @param <DC> the ServletContext specification type
 * @param <L> the HttpSessionAttributeListener specification type
 * @param <V> the attributes value type
 * @param <MV> the serialized attributes value type
 */
public abstract class MarshalledValueMarshallerSessionAttributesFactoryConfiguration<S, DC, L, V> extends MarshallerSessionAttributesFactoryConfiguration<S, DC, L, V, MarshalledValue<V, ByteBufferMarshaller>> {

	protected <SC> MarshalledValueMarshallerSessionAttributesFactoryConfiguration(SessionManagerFactoryConfiguration<S, DC, L, SC> configuration) {
		super(configuration, new MarshalledValueMarshaller<>(new ByteBufferMarshalledValueFactory(configuration.getMarshaller())));
	}
}
