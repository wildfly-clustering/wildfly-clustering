/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.service;

/**
 * A restartable service.
 * @author Paul Ferraro
 */
public interface Service {
	/**
	 * Indicates whether or not this service is started.
	 * @return true, if this service is started, false otherwise
	 */
	boolean isStarted();

	/**
	 * Starts this service.
	 */
	void start();

	/**
	 * Stops this service.
	 */
	void stop();
}
