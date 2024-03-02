/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec.servlet;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractImmutableHttpSession extends AbstractHttpSession {

	@Override
	public void setMaxInactiveInterval(int interval) {
		// Ignore
	}

	@Override
	public void setAttribute(String name, Object value) {
		// Ignore
	}

	@Override
	public void removeAttribute(String name) {
		// Ignore
	}

	@Override
	public void invalidate() {
		// Ignore
	}
}
