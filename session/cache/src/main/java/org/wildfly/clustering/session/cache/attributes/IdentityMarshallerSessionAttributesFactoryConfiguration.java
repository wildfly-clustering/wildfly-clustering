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
 * @param <V> the attributes value type
 */
public class IdentityMarshallerSessionAttributesFactoryConfiguration<V> extends AbstractSessionAttributesFactoryConfiguration<V, V> {
	/**
	 * Creates a session attributes factory configuration
	 * @param <SC> a session context type
	 * @param configuration a session manager factory configuration
	 */
	public <SC> IdentityMarshallerSessionAttributesFactoryConfiguration(SessionManagerFactoryConfiguration<SC> configuration) {
		super(configuration, Marshaller.identity());
	}
}
