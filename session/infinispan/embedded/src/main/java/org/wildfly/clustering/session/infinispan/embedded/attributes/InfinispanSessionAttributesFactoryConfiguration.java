/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.attributes;

import java.util.function.Function;

import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;

/**
 * @param <S> the HttpSession specification type
 * @param <C> the ServletContext specification type
 * @param <L> the HttpSessionActivationListener specification type
 * @param <V> attributes cache entry type
 * @param <MV> attributes serialized form type
 * @author Paul Ferraro
 */
public interface InfinispanSessionAttributesFactoryConfiguration<S, C, L, V, MV> extends SessionAttributesFactoryConfiguration<S, C, L, V, MV> {

	Function<String, SessionAttributeActivationNotifier> getActivationNotifierFactory();
}
