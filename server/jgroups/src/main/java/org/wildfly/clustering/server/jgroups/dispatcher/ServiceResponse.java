/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.jgroups.dispatcher;

/**
 * Simple object that indicates that no service is registered on the group member for which to execute the remote command.
 * @author Paul Ferraro
 */
public enum ServiceResponse {
	NO_SUCH_SERVICE;
}
