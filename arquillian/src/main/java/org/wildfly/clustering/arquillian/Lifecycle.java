/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

/**
 * @author Paul Ferraro
 */
public interface Lifecycle extends AutoCloseable {

	String getName();

	void start();

	void stop();

	boolean isStarted();

	@Override
	default void close() {
		if (this.isStarted()) {
			this.stop();
		}
	}
}
