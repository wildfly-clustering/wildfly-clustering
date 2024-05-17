/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

/**
 * A restartable service.
 * @author Paul Ferraro
 */
public interface Service {

	/**
	 * Starts this service.
	 */
	void start();

	/**
	 * Stops this service.
	 */
	void stop();
}
