/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;

/**
 * Encapsulates the parameters of the session manager integration test.
 * @author Paul Ferraro
 */
public interface SessionManagerParameters {

	default String getClusterName() {
		return "cluster";
	}

	default String getDeploymentName() {
		return String.format("%s-%s.war", this.getSessionAttributeMarshaller(), this.getSessionAttributePersistenceStrategy());
	}

	SessionAttributePersistenceStrategy getSessionAttributePersistenceStrategy();
	ByteBufferMarshaller getSessionAttributeMarshaller();
}
