/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

/**
 * Enumerates the strategies for persisting session attributes.
 * @author Paul Ferraro
 */
public enum SessionAttributePersistenceStrategy {
	/**
	 * Persists session attributes together, preserving any cross-attribute references.
	 */
	COARSE,
	/**
	 * Persists session attributes individually, precluding cross-attribute references.
	 */
	FINE,
}
