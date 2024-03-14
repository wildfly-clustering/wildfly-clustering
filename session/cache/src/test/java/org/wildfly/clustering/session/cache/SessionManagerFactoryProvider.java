/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.SessionManagerFactory;

/**
 * Provides a session manager factory to the session manager integration test.
 * @param <C> the session context type
 * @param <B> the batch type
 * @author Paul Ferraro
 */
public interface SessionManagerFactoryProvider<C, B extends Batch> extends AutoCloseable {

	<SC> SessionManagerFactory<C, SC, B> createSessionManagerFactory(Supplier<SC> contextFactory);
}
