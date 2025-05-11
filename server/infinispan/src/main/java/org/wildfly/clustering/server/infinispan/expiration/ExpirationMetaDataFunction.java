/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import org.wildfly.clustering.server.expiration.ExpirationMetaData;

/**
 * Maps expiration meta data to an optional expiration instant.
 * @author Paul Ferraro
 */
public enum ExpirationMetaDataFunction implements Function<ExpirationMetaData, Optional<Instant>> {
	INSTANCE;

	@Override
	public Optional<Instant> apply(ExpirationMetaData metaData) {
		return metaData.getExpirationTime();
	}
}
