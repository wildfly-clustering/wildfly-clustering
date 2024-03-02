/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;

/**
 * Configuration for a factory for creating {@link SessionAttributes} objects, based on marshalled values.
 * @author Paul Ferraro
 * @param <S> the HttpSession specification type
 * @param <DC> the ServletContext specification type
 * @param <L> the HttpSessionAttributeListener specification type
 * @param <V> the attributes value type
 */
public class IdentityMarshallerSessionAttributesFactoryConfiguration<V> extends MarshallerSessionAttributesFactoryConfiguration<V, V> {

	public <SC> IdentityMarshallerSessionAttributesFactoryConfiguration(SessionManagerFactoryConfiguration<SC> configuration) {
		super(configuration, Marshaller.identity());
	}
}
