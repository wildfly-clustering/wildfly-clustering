/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.dispatcher;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registration;

/**
 * Dispatches commands for execution on members of a group.
 * @param <M> the member type
 * @param <C> the command context type
 * @author Paul Ferraro
 */
public interface CommandDispatcher<M extends GroupMember, C> extends Registration {

	/**
	 * Returns the context with which this dispatcher was created.
	 * @return a command execution context
	 */
	C getContext();

	/**
	 * Executes the specified command on the specified group member.
	 * If the member has no corresponding dispatcher, the returned completion stage throws a {@link java.util.concurrent.CancellationException}.
	 *
	 * @param <R> the command execution return type
	 * @param command the command to execute
	 * @param member the group member on which to execute the command
	 * @return the future result of the command execution
	 * @throws IOException if the command could not be sent
	 */
	<R, E extends Exception> CompletionStage<R> dispatchToMember(Command<R, ? super C, E> command, M member) throws IOException;

	/**
	 * Executes the specified command on all members of the group, optionally excluding some members.
	 * If a given member has no corresponding dispatcher, its completion stage throws a {@link java.util.concurrent.CancellationException}.
	 *
	 * @param <R> the command execution return type
	 * @param command the command to execute
	 * @return a completion stage per member of the group on which the command was executed
	 * @throws IOException if the command could not be sent
	 */
	default <R, E extends Exception> Map<M, CompletionStage<R>> dispatchToGroup(Command<R, ? super C, E> command) throws IOException {
		return this.dispatchToGroup(command, Set.of());
	}

	/**
	 * Executes the specified command on all members of the group, optionally excluding some members.
	 * If a given member has no corresponding dispatcher, its completion stage throws a {@link java.util.concurrent.CancellationException}.
	 *
	 * @param <R> the command execution return type
	 * @param command the command to execute
	 * @param excluding the members to be excluded from group command execution
	 * @return a completion stage per member of the group on which the command was executed
	 * @throws IOException if the command could not be sent
	 */
	<R, E extends Exception> Map<M, CompletionStage<R>> dispatchToGroup(Command<R, ? super C, E> command, Set<M> excluding) throws IOException;
}
