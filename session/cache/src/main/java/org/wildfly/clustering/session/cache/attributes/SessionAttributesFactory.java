/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import org.wildfly.clustering.cache.Creator;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Factory for creating a {@link SessionAttributes} object.
 * @param <C> the deployment context type
 * @param <V> the marshalled value type
 * @author Paul Ferraro
 */
public interface SessionAttributesFactory<C, V> extends ImmutableSessionAttributesFactory<V>, Creator<String, V, Void>, Remover<String>, Registration {
	SessionAttributes createSessionAttributes(String id, V value, ImmutableSessionMetaData metaData, C context);
}
