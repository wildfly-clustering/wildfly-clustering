/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.jgroups.dispatcher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.jgroups.Address;
import org.jgroups.BytesMessage;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RspFilter;
import org.wildfly.clustering.server.dispatcher.Command;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.jgroups.ChannelGroup;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;

/**
 * MessageDispatcher-based command dispatcher.
 * @author Paul Ferraro
 * @param <CC> the command execution context
 * @param <MC> the marshalling context
 */
public class JChannelCommandDispatcher<CC, MC> implements CommandDispatcher<ChannelGroupMember, CC> {
	private static final System.Logger LOGGER = System.getLogger(JChannelCommandDispatcher.class.getName());

	private static final RspFilter FILTER = new RspFilter() {
		@Override
		public boolean isAcceptable(Object response, Address sender) {
			return !(response instanceof ServiceResponse);
		}

		@Override
		public boolean needMoreResponses() {
			return true;
		}
	};

	interface Configuration<CC, MC> {
		/**
		 * Returns the identifier of this dispatcher.
		 * @return the identifier of this dispatcher.
		 */
		Object getId();

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

	private final Object id;
	private final CC commandContext;
	private final MessageDispatcher dispatcher;
	private final CommandMarshaller<CC> marshaller;
	private final MC marshallingContext;
	private final ChannelGroup group;
	private final Runnable closeTask;
	private final RequestOptions options;

	/**
	 * Creates a command dispatcher using the specified configuration.
	 * @param configuration the configuration of this command dispatcher
	 */
	public JChannelCommandDispatcher(Configuration<CC, MC> configuration) {
		this.id = configuration.getId();
		this.commandContext = configuration.getCommandExecutionContext();
		this.dispatcher = configuration.getMessageDispatcher();
		this.marshaller = configuration.getCommandMarshaller();
		this.marshallingContext = configuration.getMarshallingContext();
		this.group = configuration.getGroup();
		this.closeTask = configuration.getCloseTask();
		this.options = new RequestOptions(ResponseMode.GET_ALL, configuration.getCommandExecutionTimeout().toMillis(), false, FILTER, Message.Flag.DONT_BUNDLE, Message.Flag.OOB);
	}

	@Override
	public CC getContext() {
		return this.commandContext;
	}

	@Override
	public void close() {
		this.closeTask.run();
	}

	@Override
	public <R, E extends Exception> CompletionStage<R> dispatchToMember(Command<R, ? super CC, E> command, ChannelGroupMember member) throws IOException {
		LOGGER.log(System.Logger.Level.TRACE, "{0} dispatching {1} command to {2}", this.id, command, member);
		// Bypass MessageDispatcher if target member is local
		if (this.group.getLocalMember().equals(member)) {
			return this.execute(command);
		}
		ByteBuffer buffer = this.createBuffer(command);
		Address address = member.getId();
		return this.send(buffer, address);
	}

	@Override
	public <R, E extends Exception> Map<ChannelGroupMember, CompletionStage<R>> dispatchToGroup(Command<R, ? super CC, E> command, Set<ChannelGroupMember> excluding) throws IOException {
		LOGGER.log(System.Logger.Level.TRACE, "{0} dispatching {1} to group, excluding %s", this.id, command, excluding);
		Map<ChannelGroupMember, CompletionStage<R>> results = new ConcurrentHashMap<>();
		ByteBuffer buffer = this.createBuffer(command);
		for (ChannelGroupMember member : this.group.getMembership().getMembers()) {
			if (!excluding.contains(member)) {
				if (this.group.getLocalMember().equals(member)) {
					results.put(member, this.execute(command));
				} else {
					Address address = member.getId();
					try {
						CompletionStage<R> result = this.send(buffer, address);
						// Don't chain - we want returned stage to throw a CancellationException when necessary.
						result.whenComplete(new PruneCancellationTask<>(results, member));
						results.put(member, result);
					} catch (IOException e) {
						// Cancel previously dispatched messages
						for (CompletionStage<R> result : results.values()) {
							result.toCompletableFuture().cancel(true);
						}
						throw e;
					}
				}
			}
		}
		return results;
	}

	private <R> CompletionStage<R> send(ByteBuffer buffer, Address address) throws IOException {
		try {
			Message message = this.createMessage(buffer, address);
			ServiceRequest<R, MC> request = new ServiceRequest<>(this.dispatcher.getCorrelator(), address, this.options, this.marshallingContext);
			request.sendRequest(message);
			return request;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private <R, E extends Exception> CompletionStage<R> execute(Command<R, ? super CC, E> command) {
		try {
			return CompletableFuture.completedStage(command.execute(this.commandContext));
		} catch (Exception e) {
			return CompletableFuture.failedStage(e);
		}
	}

	private <R, E extends Exception> ByteBuffer createBuffer(Command<R, ? super CC, E> command) {
		try {
			return this.marshaller.marshal(command);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Message createMessage(ByteBuffer buffer, Address destination) {
		return new BytesMessage().setArray(buffer.array(), buffer.arrayOffset(), buffer.limit() - buffer.arrayOffset()).src(this.group.getLocalMember().getId()).dest(destination);
	}

	private static class PruneCancellationTask<R> implements BiConsumer<R, Throwable> {
		private final Map<ChannelGroupMember, CompletionStage<R>> results;
		private final ChannelGroupMember member;

		PruneCancellationTask(Map<ChannelGroupMember, CompletionStage<R>> results, ChannelGroupMember member) {
			this.results = results;
			this.member = member;
		}

		@Override
		public void accept(R result, Throwable exception) {
			if (exception instanceof CancellationException) {
				this.results.remove(this.member);
			}
		}
	}
}
