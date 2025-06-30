/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

/**
 * @author Paul Ferraro
 */
public interface Contextual {
	/**
	 * Returns the name of this context.
	 * @return the name of this context.
	 */
	String getName();

	/**
	 * Attach this context to the specified exception.
	 * @param e an exception to which to attach this context.
	 */
	void attach(Throwable e);
}
