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

	private final CC commandContext;
	private final MessageDispatcher dispatcher;
	private final CommandMarshaller<CC> marshaller;
	private final MC marshallingContext;
	private final ChannelGroup group;
	private final Duration timeout;
	private final Runnable closeTask;
	private final RequestOptions options;

	public JChannelCommandDispatcher(CC commandContext, MessageDispatcher dispatcher, CommandMarshaller<CC> marshaller, MC marshallingContext, ChannelGroup group, Duration timeout, Runnable closeTask) {
		this.commandContext = commandContext;
		this.dispatcher = dispatcher;
		this.marshaller = marshaller;
		this.marshallingContext = marshallingContext;
		this.group = group;
		this.timeout = timeout;
		this.closeTask = closeTask;
		this.options = new RequestOptions(ResponseMode.GET_ALL, this.timeout.toMillis(), false, FILTER, Message.Flag.DONT_BUNDLE, Message.Flag.OOB);
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
		// Bypass MessageDispatcher if target member is local
		if (this.group.getLocalMember().equals(member)) {
			return this.execute(command);
		}
		ByteBuffer buffer = this.createBuffer(command);
		Address address = member.getId();
		Message message = this.createMessage(buffer, address);
		ServiceRequest<R, MC> request = new ServiceRequest<>(this.dispatcher.getCorrelator(), address, this.options, this.marshallingContext);
		return request.send(message);
	}

	@Override
	public <R, E extends Exception> Map<ChannelGroupMember, CompletionStage<R>> dispatchToGroup(Command<R, ? super CC, E> command, Set<ChannelGroupMember> excluding) throws IOException {
		Map<ChannelGroupMember, CompletionStage<R>> results = new ConcurrentHashMap<>();
		ByteBuffer buffer = this.createBuffer(command);
		for (ChannelGroupMember member : this.group.getMembership().getMembers()) {
			if (!excluding.contains(member)) {
				if (this.group.getLocalMember().equals(member)) {
					results.put(member, this.execute(command));
				} else {
					Address address = member.getId();
					try {
						ServiceRequest<R, MC> request = new ServiceRequest<>(this.dispatcher.getCorrelator(), address, this.options, this.marshallingContext);
						Message message = this.createMessage(buffer, address);
						CompletionStage<R> result = request.send(message);
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
