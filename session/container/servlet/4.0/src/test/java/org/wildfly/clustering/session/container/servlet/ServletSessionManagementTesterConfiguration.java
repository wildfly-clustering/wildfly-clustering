/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import org.wildfly.clustering.session.container.SessionManagementTesterConfiguration;

/**
 * @author Paul Ferraro
 */
public interface ServletSessionManagementTesterConfiguration extends SessionManagementTesterConfiguration {

	@Override
	default Class<?> getEndpointClass() {
		return SessionServlet.class;
	}
}
