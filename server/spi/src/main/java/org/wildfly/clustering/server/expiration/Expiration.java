/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.expiration;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Describes the expiration criteria for a managed object.
 * @author Paul Ferraro
 */
public interface Expiration {
	/**
	 * The duration of time, after which an idle object should expire.
	 * @return the object timeout
	 */
	Duration getTimeout();

	/**
	 * Indicates whether the associated timeout represents and immortal object,
	 * i.e. does not expire
	 * @return true, if this object is immortal, false otherwise
	 */
	default boolean isImmortal() {
		return Optional.ofNullable(this.getTimeout()).filter(Predicate.not(Duration::isZero).and(Predicate.not(Duration::isNegative))).isEmpty();
	}
}
