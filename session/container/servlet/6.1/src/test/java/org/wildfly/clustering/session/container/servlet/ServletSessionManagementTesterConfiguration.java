/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import org.wildfly.clustering.session.container.SessionManagementTesterConfiguration;
import org.wildfly.clustering.session.container.servlet.atomic.AtomicSessionServlet;

/**
 * @author Paul Ferraro
 */
public interface ServletSessionManagementTesterConfiguration extends SessionManagementTesterConfiguration {

	@Override
	default Class<?> getEndpointClass() {
		return AtomicSessionServlet.class;
	}
}
