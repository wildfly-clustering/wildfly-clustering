/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.jgroups.dispatcher;

/**
 * Enumeration of fixed command dispatcher responses.
 * @author Paul Ferraro
 */
public enum ServiceResponse {
	/** A response indicating that the recipient could not execute a given command as the associated service is not recognized. */
	NO_SUCH_SERVICE
}
