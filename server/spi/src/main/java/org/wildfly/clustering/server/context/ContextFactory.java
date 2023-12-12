/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.context;

import java.util.function.Consumer;

/**
 * Creates a context for managing references to server-side state.
 * @author Paul Ferraro
 */
public interface ContextFactory {

	<K, V> Context<K, V> createContext(Consumer<V> startTask, Consumer<V> stopTask);
}
