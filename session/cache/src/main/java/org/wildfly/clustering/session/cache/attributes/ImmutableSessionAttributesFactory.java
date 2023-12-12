/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.Locator;
import org.wildfly.clustering.session.ImmutableSessionAttributes;

/**
 * Factory for creating {@link ImmutableSessionAttributes} objects.
 * @author Paul Ferraro
 * @param <V> attributes cache entry type
 */
public interface ImmutableSessionAttributesFactory<V> extends Locator<String, V> {
	ImmutableSessionAttributes createImmutableSessionAttributes(String id, V value);

	default CompletionStage<ImmutableSessionAttributes> createImmutableSessionAttributes(String id, CompletionStage<V> value) {
		return value.thenApply(v -> (v != null) ? this.createImmutableSessionAttributes(id, v) : null);
	}
}
