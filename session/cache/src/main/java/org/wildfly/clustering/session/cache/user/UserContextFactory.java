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
 * @param <V> the cache value type
 * @param <PC> the persistent context type
 * @param <TC> the transient context type
 * @author Paul Ferraro
 */
public interface UserContextFactory<V, PC, TC> extends Creator<String, V, PC>, Locator<String, V>, Remover<String> {

	Map.Entry<PC, TC> createUserContext(V value);

	default CompletionStage<Map.Entry<PC, TC>> createUserContext(CompletionStage<V> value) {
		return value.thenApply(this::createUserContext);
	}
}
