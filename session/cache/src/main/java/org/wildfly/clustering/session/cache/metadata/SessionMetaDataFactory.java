/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.cache.Creator;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.server.Registration;

/**
 * @author Paul Ferraro
 */
public interface SessionMetaDataFactory<V> extends ImmutableSessionMetaDataFactory<V>, Creator<String, V, Duration>, Remover<String>, Registration {
	InvalidatableSessionMetaData createSessionMetaData(String id, V value);

	default CompletionStage<InvalidatableSessionMetaData> createSessionMetaData(String id, CompletionStage<V> value) {
		return value.thenApply(v -> (v != null) ? this.createSessionMetaData(id, v) : null);
	}
}
