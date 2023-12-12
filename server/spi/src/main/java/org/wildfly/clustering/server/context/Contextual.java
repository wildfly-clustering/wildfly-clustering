/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.context;

/**
 * Implemented by contextual objects.
 * @author Paul Ferraro
 */
public interface Contextual {

	/**
	 * Indicates the end of this object's contextual lifecycle.
	 */
	void end();
}
