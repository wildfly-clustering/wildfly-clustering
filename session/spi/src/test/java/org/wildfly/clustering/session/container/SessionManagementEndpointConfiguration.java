/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

/**
 * @author Paul Ferraro
 */
public interface SessionManagementEndpointConfiguration {
	String ENDPOINT_NAME = "session";
	String ENDPOINT_PATH = "/" + ENDPOINT_NAME;
	String COUNTER = "counter";
	String IMMUTABLE = "immutable";
	String SESSION_ID = "session-id";
}
