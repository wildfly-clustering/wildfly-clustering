/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.function.Supplier;

import org.wildfly.clustering.session.SessionManagerFactory;

/**
 * Provides a session manager factory to the session manager integration test.
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public interface SessionManagerFactoryProvider<C> extends AutoCloseable {

	<SC> SessionManagerFactory<C, SC> createSessionManagerFactory(Supplier<SC> contextFactory);
}
