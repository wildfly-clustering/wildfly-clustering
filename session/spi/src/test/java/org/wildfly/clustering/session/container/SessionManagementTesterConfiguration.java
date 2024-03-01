/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.net.http.HttpClient;
import java.util.function.UnaryOperator;

/**
 * @author Paul Ferraro
 */
public interface SessionManagementTesterConfiguration extends SessionManagementEndpointConfiguration {

	default UnaryOperator<HttpClient.Builder> getHttpClientConfigurator() {
		return UnaryOperator.identity();
	}

	default boolean isTransactional() {
		return false;
	}
}
