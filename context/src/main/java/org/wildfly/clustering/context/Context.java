/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.context;

/**
 * Encapsulates some context that is applicable until {@link #close()}.
 * @author Paul Ferraro
 */
public interface Context extends AutoCloseable {
	@Override
	void close();
}
