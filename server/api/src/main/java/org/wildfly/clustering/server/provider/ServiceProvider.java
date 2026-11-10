/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.provider;

/**
 * Encapsulates a service provider.
 * @author Paul Ferraro
 * @param <T> the provided service type
 */
public interface ServiceProvider<T> {
	/**
	 * Returns the provided service.
	 * @return the provided service.
	 */
	T getService();
}
