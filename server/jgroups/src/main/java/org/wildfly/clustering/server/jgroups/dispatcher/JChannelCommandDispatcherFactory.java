/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.jgroups.dispatcher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestCorrelator;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.Response;
import org.jgroups.protocols.RSVP;
import org.jgroups.stack.ProtocolStack;
import org.wildfly.clustering.context.Contextualizer;
import org.wildfly.clustering.context.DefaultContextualizerFactory;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledValueFactory;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshalledValue;
import org.wildfly.clustering.marshalling.MarshalledValueFactory;
import org.wildfly.clustering.server.dispatcher.Command;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.ChannelGroup;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.server.jgroups.JChannelGroup;
import org.wildfly.clustering.server.util.BlockingExecutor;
import org.wildfly.common.function.ExceptionSupplier;
import org.wildfly.common.function.Functions;

/**
 * {@link MessageDispatcher} based {@link CommandDispatcherFactory}.
 * This factory can produce multiple {@link CommandDispatcher} instances,
 * all of which will share the same {@link MessageDispatcher} instance.
 * @author Paul Ferraro
 */
public class JChannelCommandDispatcherFactory implements ChannelCommandDispatcherFactory, RequestHandler, Runnable {
	private static final Optional<Object> NO_SUCH_SERVICE = Optional.of(ServiceResponse.NO_SUCH_SERVICE);
	private static final ExceptionSupplier<Object, Exception> NO_SUCH_SERVICE_SUPPLIER = Functions.constantExceptionSupplier(ServiceResponse.NO_SUCH_SERVICE);

	private final JChannelGroup group;
	private final Map<Object, CommandDispatcherContext<?, ?>> contexts = new ConcurrentHashMap<>();
	private final BlockingExecutor executor = BlockingExecutor.newInstance(this);
	private final ByteBufferMarshaller marshaller;
	private final MessageDispatcher dispatcher;
	private final Duration timeout;
	private final Function<ClassLoader, ByteBufferMarshaller> marshallerFactory;

	@SuppressWarnings("resource")
	public JChannelCommandDispatcherFactory(JChannelCommandDispatcherFactoryConfiguration config) {
		this.marshaller = config.getMarshaller();
		this.marshallerFactory = config.getMarshallerFactory();
		JChannel channel = config.getChannel();
		ProtocolStack stack = channel.getProtocolStack();
		RSVP rsvp = stack.findProtocol(RSVP.class);
		this.timeout = Duration.ofMillis((rsvp != null) ? rsvp.getTimeout() : stack.getTransport().getWhoHasCacheTimeout());
		this.group = new JChannelGroup(channel);
		RequestCorrelator correlator = new CommandDispatcherRequestCorrelator(channel, this, config);
		this.dispatcher = new MessageDispatcher()
				.setChannel(channel)
				.setRequestHandler(this)
				.setReceiver(this.group)
				.asyncDispatching(true)
				// Setting the request correlator starts the dispatcher
				.correlator(correlator)
				;
	}

	@Override
	public void run() {
		this.dispatcher.stop();
		this.dispatcher.getChannel().setUpHandler(null);
		this.group.close();
	}

	@Override
	public void close() {
		this.executor.close();
	}

	@Override
	public Object handle(Message request) throws Exception {
		return this.read(request).get();
	}

	@Override
	public void handle(Message request, Response response) throws Exception {
		ExceptionSupplier<Object, Exception> commandTask = this.read(request);
		Runnable responseTask = new Runnable() {
			@Override
			public void run() {
				try {
					response.send(commandTask.get(), false);
				} catch (Throwable e) {
					response.send(e, true);
				}
			}
		};
		try {
			this.dispatcher.getChannel().getProtocolStack().getTransport().getThreadPool().getThreadPool().execute(responseTask);
		} catch (RejectedExecutionException e) {
			response.send(ServiceResponse.NO_SUCH_SERVICE, false);
		}
	}

	private ExceptionSupplier<Object, Exception> read(Message message) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(message.getArray(), message.getOffset(), message.getLength());
		@SuppressWarnings("unchecked")
		Map.Entry<Object, MarshalledValue<Command<Object, Object, Exception>, Object>> entry = (Map.Entry<Object, MarshalledValue<Command<Object, Object, Exception>, Object>>) this.marshaller.read(buffer);
		Object clientId = entry.getKey();
		CommandDispatcherContext<?, ?> context = this.contexts.get(clientId);
		if (context == null) return NO_SUCH_SERVICE_SUPPLIER;
		Object commandContext = context.getCommandContext();
		Contextualizer contextualizer = context.getContextualizer();
		MarshalledValue<Command<Object, Object, Exception>, Object> value = entry.getValue();
		Command<Object, Object, Exception> command = value.get(context.getMarshalledValueFactory().getMarshallingContext());
		ExceptionSupplier<Object, Exception> commandExecutionTask = new ExceptionSupplier<>() {
			@Override
			public Object get() throws Exception {
				return context.getMarshalledValueFactory().createMarshalledValue(command.execute(commandContext));
			}
		};
		BlockingExecutor executor = this.executor;
		return new ExceptionSupplier<>() {
			@Override
			public Object get() throws Exception {
				return executor.execute(contextualizer.contextualize(commandExecutionTask)).orElse(NO_SUCH_SERVICE);
			}
		};
	}

	@Override
	public ChannelGroup getGroup() {
		return this.group;
	}

	@Override
	public <C> CommandDispatcher<ChannelGroupMember, C> createCommandDispatcher(Object id, C commandContext, ClassLoader loader) {
		ByteBufferMarshaller dispatcherMarshaller = this.marshallerFactory.apply(loader);
		MarshalledValueFactory<ByteBufferMarshaller> factory = new ByteBufferMarshalledValueFactory(dispatcherMarshaller);
		Contextualizer contextualizer = DefaultContextualizerFactory.INSTANCE.createContextualizer(loader);
		CommandDispatcherContext<C, ByteBufferMarshaller> context = new CommandDispatcherContext<>() {
			@Override
			public C getCommandContext() {
				return commandContext;
			}

			@Override
			public Contextualizer getContextualizer() {
				return contextualizer;
			}

			@Override
			public MarshalledValueFactory<ByteBufferMarshaller> getMarshalledValueFactory() {
				return factory;
			}
		};
		if (this.contexts.putIfAbsent(id, context) != null) {
			throw new IllegalArgumentException(id.toString());
		}
		CommandMarshaller<C> marshaller = new CommandDispatcherMarshaller<>(this.marshaller, id, factory);
		return new JChannelCommandDispatcher<>(commandContext, this.dispatcher, marshaller, dispatcherMarshaller, this.group, this.timeout, () -> this.contexts.remove(id));
	}
}
