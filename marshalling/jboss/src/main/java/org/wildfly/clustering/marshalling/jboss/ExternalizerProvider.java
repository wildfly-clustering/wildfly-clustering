/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.jboss.marshalling.Externalizer;

/**
 * Service provider interface for contributed externalizers.
 * @author Paul Ferraro
 */
public interface ExternalizerProvider {

	/**
	 * Returns the externalized type
	 * @return the externalized type
	 */
	Class<?> getType();

	/**
	 * Returns the provided externalizer
	 * @return the provided externalizer
	 */
	Externalizer getExternalizer();
}
