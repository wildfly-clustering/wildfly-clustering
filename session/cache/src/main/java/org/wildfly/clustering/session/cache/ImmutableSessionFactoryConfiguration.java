/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.session.cache.attributes.ImmutableSessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.ImmutableSessionMetaDataFactory;

/**
 * The configuration of an immutable session factory.
 * @param <MV> the session metadata type
 * @param <AV> the session attributes type
 */
public interface ImmutableSessionFactoryConfiguration<MV, AV> {
	/**
	 * Returns the session meta data factory.
	 * @return the session meta data factory.
	 */
	ImmutableSessionMetaDataFactory<MV> getSessionMetaDataFactory();

	/**
	 * Returns the session attributes factory.
	 * @return the session attributes factory.
	 */
	ImmutableSessionAttributesFactory<AV> getSessionAttributesFactory();

	/**
	 * Returns the properties of the associated cache.
	 * @return the properties of the associated cache.
	 */
	CacheProperties getCacheProperties();
}
