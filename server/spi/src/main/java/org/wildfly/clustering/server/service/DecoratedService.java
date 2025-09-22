/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.service;

/**
 * A {@link Service} decorator.
 * @author Paul Ferraro
 */
public class DecoratedService implements Service {

	private final Service service;

	/**
	 * Creates a decorator of the specified service.
	 * @param service the decorated service
	 */
	public DecoratedService(Service service) {
		this.service = service;
	}

	@Override
	public boolean isStarted() {
		return this.service.isStarted();
	}

	@Override
	public void start() {
		this.service.start();
	}

	@Override
	public void stop() {
		this.service.stop();
	}
}
