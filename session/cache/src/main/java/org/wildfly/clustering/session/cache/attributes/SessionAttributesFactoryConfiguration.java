/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.marshalling.Marshaller;
import org.wildfly.clustering.server.immutable.Immutability;
import org.wildfly.clustering.session.container.SessionActivationListenerFacadeProvider;

/**
 * Configuration of a factory for creating a {@link SessionAttributes} object.
 * @param <S> the HttpSession specification type
 * @param <C> the ServletContext specification type
 * @param <L> the HttpSessionActivationListener specification type
 * @param <V> attributes cache entry type
 * @param <MV> attributes serialized form type
 * @author Paul Ferraro
 */
public interface SessionAttributesFactoryConfiguration<S, C, L, V, MV> {
	Marshaller<V, MV> getMarshaller();
	Immutability getImmutability();
	SessionActivationListenerFacadeProvider<S, C, L> getSessionActivationListenerProvider();
}
