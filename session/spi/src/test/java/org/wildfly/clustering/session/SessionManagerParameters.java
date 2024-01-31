/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

/**
 * Encapsulates the parameters of the session manager integration test.
 * @author Paul Ferraro
 */
public interface SessionManagerParameters {

	default String getClusterName() {
		return "cluster";
	}

	SessionAttributePersistenceStrategy getSessionAttributePersistenceStrategy();
}
