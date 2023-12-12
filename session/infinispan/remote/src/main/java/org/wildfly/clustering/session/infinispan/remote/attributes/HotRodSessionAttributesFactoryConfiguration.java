/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.attributes;

import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactoryConfiguration;

/**
 * @author Paul Ferraro
 * @param <S> the HttpSession specification type
 * @param <C> the ServletContext specification type
 * @param <L> the HttpSessionActivationListener specification type
 * @param <V> attributes cache entry type
 * @param <SV> attributes serialized form type
 */
public interface HotRodSessionAttributesFactoryConfiguration<S, C, L, V, SV> extends SessionAttributesFactoryConfiguration<S, C, L, V, SV>, RemoteCacheConfiguration {
}
