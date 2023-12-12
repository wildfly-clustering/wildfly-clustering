/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.context;

import org.wildfly.clustering.server.Registration;

/**
 * A registration facade that reinstruments its lifecycle.
 * @author Paul Ferraro
 */
public class ContextualRegistration implements Contextual, Registration {
	private final Registration registration;
	private final Runnable closeTask;

	public ContextualRegistration(Registration registration, Runnable closeTask) {
		this.registration = registration;
		this.closeTask = closeTask;
	}

	@Override
	public void end() {
		this.registration.close();
	}

	@Override
	public void close() {
		this.closeTask.run();
	}
}
