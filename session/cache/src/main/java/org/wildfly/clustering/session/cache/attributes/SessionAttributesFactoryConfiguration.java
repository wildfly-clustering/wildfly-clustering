/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;

/**
 * Configuration of a factory for creating a {@link SessionAttributes} object.
 * @param <V> attributes cache entry type
 * @param <MV> attributes serialized form type
 * @author Paul Ferraro
 */
public interface SessionAttributesFactoryConfiguration<V, MV> {
	/**
	 * Returns the marshaller for the attributes of a session.
	 * @return the marshaller for the attributes of a session.
	 */
	Marshaller<V, MV> getMarshaller();

	/**
	 * Returns the immutability predicate for the attributes of a session.
	 * @return the immutability predicate for the attributes of a session.
	 */
	Immutability getImmutability();
}
