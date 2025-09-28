/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import java.time.Duration;

import org.jgroups.blocks.MessageDispatcher;
import org.wildfly.clustering.server.jgroups.ChannelGroup;

/**
 * The configuration of a {@link JChannelCommandDispatcher}.
 * @author Paul Ferraro
 * @param <CC> the command execution context type
 * @param <MC> the marshalling context type
 */
public interface JChannelCommandDispatcherConfiguration<CC, MC> {

	/**
	 * Returns the command context of this command dispatcher.
	 * @return the command context of this command dispatcher.
	 */
	CC getCommandExecutionContext();

	/**
	 * Returns the message dispatcher associated with this command dispatcher.
	 * @return the message dispatcher associated with this command dispatcher.
	 */
	MessageDispatcher getMessageDispatcher();

	/**
	 * Returns the marshaller for dispatched commands.
	 * @return the marshaller for dispatched commands.
	 */
	CommandMarshaller<CC> getCommandMarshaller();

	/**
	 * Returns the marshalling context of this command dispatcher.
	 * @return the marshalling context of this command dispatcher.
	 */
	MC getMarshallingContext();

	/**
	 * Returns the group associated with this command dispatcher.
	 * @return the group associated with this command dispatcher.
	 */
	ChannelGroup getGroup();

	/**
	 * Returns the maximum duration permitted for command execution.
	 * @return the maximum duration permitted for command execution.
	 */
	Duration getCommandExecutionTimeout();

	/**
	 * Returns the task to execute on {@link JChannelCommandDispatcher#close}
	 * @return the task to execute on {@link JChannelCommandDispatcher#close}
	 */
	Runnable getCloseTask();
}
