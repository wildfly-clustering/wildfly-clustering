/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.util.Map;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.container.ContainerFacadeProvider;

/**
 * Provides a session manager factory to the session manager integration test.
 * @author Paul Ferraro
 */
public interface SessionManagerFactoryProvider<DC, B extends Batch> extends AutoCloseable {

	<SC> SessionManagerFactory<DC, SC, B> createSessionManagerFactory(Supplier<SC> contextFactory, ContainerFacadeProvider<Map.Entry<ImmutableSession, DC>, DC, PassivationListener<DC>> provider);
}
