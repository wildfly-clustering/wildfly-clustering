/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.listener;

import java.util.function.Consumer;

import org.wildfly.clustering.server.Registrar;

/**
 * Registrar for a listener.
 * @param <T> the listener type
 * @author Paul Ferraro
 */
public interface ListenerRegistry<T> extends Registrar<T>, Consumer<Consumer<T>>, AutoCloseable {

	@Override
	void close();
}
