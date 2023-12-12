/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.io.IOException;

import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.KeyScalarMarshaller;
import org.wildfly.common.function.ExceptionFunction;

/**
 * Generic marshaller for cache keys containing session identifiers.
 * @author Paul Ferraro
 */
public class SessionKeyMarshaller<K extends Key<String>> extends KeyScalarMarshaller<String, K> {

	public SessionKeyMarshaller(Class<K> targetClass, ExceptionFunction<String, K, IOException> resolver) {
		super(targetClass, IdentifierMarshaller.INSTANCE, resolver);
	}
}
