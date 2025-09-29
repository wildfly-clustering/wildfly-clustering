/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.user;

import java.util.function.Supplier;

import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.server.manager.ManagerConfiguration;

/**
 * Encapsulates the configuration of a user manager.
 * @author Paul Ferraro
 * @param <T> the transient context type
 */
public interface UserManagerConfiguration<T> extends ManagerConfiguration<String> {
	/**
	 * Returns the marshaller used to marshal the persistent user context.
	 * @return the marshaller used to marshal the persistent user context.
	 */
	ByteBufferMarshaller getMarshaller();

	/**
	 * Returns a factory for creating the transient context of a user.
	 * @return a factory for creating the transient context of a user.
	 */
	Supplier<T> getTransientContextFactory();
}
