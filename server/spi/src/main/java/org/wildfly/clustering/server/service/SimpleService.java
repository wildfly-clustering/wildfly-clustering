/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.service;

/**
 * A simple service with no lifecycle behavior.
 * @author Paul Ferraro
 */
public class SimpleService implements Service {
	private volatile boolean started;

	/**
	 * Creates a simple service
	 */
	public SimpleService() {
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}

	@Override
	public void start() {
		this.started = true;
	}

	@Override
	public void stop() {
		this.started = false;
	}
}
