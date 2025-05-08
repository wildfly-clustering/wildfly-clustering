/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.ResourceBundle;

/**
 * @author Paul Ferraro
 */
enum Logger implements System.Logger {
	INSTANCE;

	private final System.Logger logger = System.getLogger(Marshaller.class.getName());

	@Override
	public String getName() {
		return this.logger.getName();
	}

	@Override
	public boolean isLoggable(Level level) {
		return this.logger.isLoggable(level);
	}

	@Override
	public void log(Level level, ResourceBundle bundle, String message, Throwable exception) {
		this.logger.log(level, bundle, message, exception);
	}

	@Override
	public void log(Level level, ResourceBundle bundle, String format, Object... params) {
		this.logger.log(level, bundle, format, params);
	}
}
