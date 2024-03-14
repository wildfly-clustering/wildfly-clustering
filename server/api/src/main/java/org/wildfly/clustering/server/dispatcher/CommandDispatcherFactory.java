/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.dispatcher;

import java.util.Optional;

import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;

/**
 * Factory for creating a command dispatcher.
 * @param <M> the member type
 * @author Paul Ferraro
 */
public interface CommandDispatcherFactory<M extends GroupMember> {

	/**
	 * Returns the group upon which the this command dispatcher operates.
	 *
	 * @return a group
	 */
	Group<M> getGroup();

	/**
	 * Creates a new command dispatcher using the specified identifier and context.
	 * The resulting {@link CommandDispatcher} will communicate with those dispatchers within the group sharing the same identifier.
	 *
	 * @param id a unique identifier for this dispatcher
	 * @param context the context used for executing commands
	 * @return a new command dispatcher
	 */
	default <C> CommandDispatcher<M, C> createCommandDispatcher(Object id, C context) {
		return this.createCommandDispatcher(id, context, Optional.ofNullable(Thread.currentThread().getContextClassLoader()).orElseGet(ClassLoader::getSystemClassLoader));
	}

	/**
	 * Creates a new command dispatcher using the specified identifier and context whose marshaller will be configured from the specified class loader.
	 * The resulting {@link CommandDispatcher} will communicate with those dispatchers within the group sharing the same identifier.
	 *
	 * @param id a unique identifier for this dispatcher
	 * @param context the context used for executing commands
	 * @param loader the class loader used to load commands to be dispatched.
	 * @return a new command dispatcher
	 */
	<C> CommandDispatcher<M, C> createCommandDispatcher(Object id, C context, ClassLoader loader);
}
