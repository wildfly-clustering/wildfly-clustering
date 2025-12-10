/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;

/**
 * The configuration of an immutable session factory.
 * @param <DC> the deployment context type
 * @param <MV> the session metadata value type
 * @param <AV> the session attributes value type
 * @param <SC> the session context type
 */
public interface SessionFactoryConfiguration<DC, MV, AV, SC> extends ImmutableSessionFactoryConfiguration<MV, AV> {
	@Override
	SessionMetaDataFactory<MV> getSessionMetaDataFactory();

	@Override
	SessionAttributesFactory<DC, AV> getSessionAttributesFactory();

	/**
	 * Returns a session context factory.
	 * @return a session context factory.
	 */
	Supplier<SC> getSessionContextFactory();
}
