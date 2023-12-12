/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.Creator;
import org.wildfly.clustering.cache.Locator;
import org.wildfly.clustering.cache.Remover;

/**
 * @author Paul Ferraro
 */
public interface UserContextFactory<V, C, L> extends Creator<String, V, C>, Locator<String, V>, Remover<String> {

	Map.Entry<C, L> createUserContext(V value);

	default CompletionStage<Map.Entry<C, L>> createUserContext(CompletionStage<V> value) {
		return value.thenApply(this::createUserContext);
	}
}
