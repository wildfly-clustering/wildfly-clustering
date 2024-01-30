/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.dispatcher.test.IdentityCommand;
import org.wildfly.common.function.ExceptionBiFunction;

/**
 * @author Paul Ferraro
 */
public abstract class CommandDispatcherITCase<M extends GroupMember> {
	private static final String CLUSTER_NAME = "cluster";

	private final ExceptionBiFunction<String, String, CommandDispatcherFactoryProvider<M>, Exception> factory;

	protected CommandDispatcherITCase(ExceptionBiFunction<String, String, CommandDispatcherFactoryProvider<M>, Exception> factory) {
		this.factory = factory;
	}

	@Test
	public void test() throws Exception {
		try (CommandDispatcherFactoryProvider<M> provider1 = this.factory.apply(CLUSTER_NAME, "member1")) {
			CommandDispatcherFactory<M> factory1 = provider1.getCommandDispatcherFactory();
			Group<M> group1 = factory1.getGroup();
			UUID fooContext1 = UUID.randomUUID();

			try (CommandDispatcher<M, UUID> dispatcher1 = factory1.createCommandDispatcher("foo", fooContext1)) {

				assertSame(fooContext1, dispatcher1.getContext());
				assertSame(fooContext1, dispatcher1.dispatchToMember(new IdentityCommand<>(), group1.getLocalMember()).toCompletableFuture().get());

				Map<M, CompletionStage<UUID>> results = dispatcher1.dispatchToGroup(new IdentityCommand<>());
				assertEquals(1, results.size());
				assertSame(fooContext1, results.get(group1.getLocalMember()).toCompletableFuture().get());

				assertTrue(dispatcher1.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember())).isEmpty());

				try (CommandDispatcherFactoryProvider<M> provider2 = this.factory.apply(CLUSTER_NAME, "member2")) {
					CommandDispatcherFactory<M> factory2 = provider2.getCommandDispatcherFactory();
					Group<M> group2 = factory2.getGroup();
					UUID fooContext2 = UUID.randomUUID();

					try (CommandDispatcher<M, UUID> dispatcher2 = factory2.createCommandDispatcher("foo", fooContext2)) {

						assertSame(fooContext2, dispatcher2.getContext());
						assertSame(fooContext2, dispatcher2.dispatchToMember(new IdentityCommand<>(), group2.getLocalMember()).toCompletableFuture().get());
						assertEquals(fooContext1, dispatcher2.dispatchToMember(new IdentityCommand<>(), group1.getLocalMember()).toCompletableFuture().get());

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>());
						assertEquals(2, results.size());
						assertEquals(fooContext1, results.get(group1.getLocalMember()).toCompletableFuture().get());
						assertEquals(fooContext2, results.get(group2.getLocalMember()).toCompletableFuture().get());

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember()));
						assertEquals(1, results.size());
						assertSame(fooContext2, results.get(group2.getLocalMember()).toCompletableFuture().get());

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group2.getLocalMember()));
						assertEquals(1, results.size());
						assertEquals(fooContext1, results.get(group1.getLocalMember()).toCompletableFuture().get());

						assertTrue(dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember(), group2.getLocalMember())).isEmpty());
					}

					results = dispatcher1.dispatchToGroup(new IdentityCommand<>());
					assertEquals(2, results.size());
					assertSame(fooContext1, results.get(group1.getLocalMember()).toCompletableFuture().get());

					assertThrows(CancellationException.class, results.get(group2.getLocalMember()).toCompletableFuture()::get);

					results = dispatcher1.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember()));
					assertEquals(1, results.size());
					assertThrows(CancellationException.class, results.get(group2.getLocalMember()).toCompletableFuture()::get);

					assertThrows(CancellationException.class, dispatcher1.dispatchToMember(new IdentityCommand<>(), group2.getLocalMember()).toCompletableFuture()::get);

					UUID barContext2 = UUID.randomUUID();

					try (CommandDispatcher<M, UUID> dispatcher2 = factory2.createCommandDispatcher("bar", barContext2)) {

						assertSame(barContext2, dispatcher2.getContext());

						assertSame(barContext2, dispatcher2.dispatchToMember(new IdentityCommand<>(), group2.getLocalMember()).toCompletableFuture().get());
						assertThrows(CancellationException.class, dispatcher2.dispatchToMember(new IdentityCommand<>(), group1.getLocalMember()).toCompletableFuture()::get);

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>());
						assertEquals(2, results.size());
						assertThrows(CancellationException.class, results.get(group1.getLocalMember()).toCompletableFuture()::get);
						assertSame(barContext2, results.get(group2.getLocalMember()).toCompletableFuture().get());

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group2.getLocalMember()));
						assertEquals(1, results.size());
						assertThrows(CancellationException.class, results.get(group1.getLocalMember()).toCompletableFuture()::get);
					}
				}
			}
		}
	}
}
