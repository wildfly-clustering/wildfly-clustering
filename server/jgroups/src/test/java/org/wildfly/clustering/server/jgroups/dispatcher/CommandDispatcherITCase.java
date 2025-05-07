/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.Group;
import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.server.jgroups.dispatcher.test.IdentityCommand;

/**
 * Base integration test for {@link CommandDispatcher} implementations.
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public abstract class CommandDispatcherITCase<M extends GroupMember> {
	private static final String CLUSTER_NAME = "cluster";

	private final BiFunction<String, String, CommandDispatcherFactoryProvider<M>> factory;

	protected CommandDispatcherITCase(BiFunction<String, String, CommandDispatcherFactoryProvider<M>> factory) {
		this.factory = factory;
	}

	@Test
	public void test() throws IOException {
		try (CommandDispatcherFactoryProvider<M> provider1 = this.factory.apply(CLUSTER_NAME, "member1")) {
			CommandDispatcherFactory<M> factory1 = provider1.getCommandDispatcherFactory();
			Group<M> group1 = factory1.getGroup();
			UUID fooContext1 = UUID.randomUUID();

			try (CommandDispatcher<M, UUID> dispatcher1 = factory1.createCommandDispatcher("foo", fooContext1)) {

				assertThat(dispatcher1.getContext()).isSameAs(fooContext1);
				assertThat(dispatcher1.dispatchToMember(new IdentityCommand<>(), group1.getLocalMember()).toCompletableFuture().join()).isSameAs(fooContext1);

				Map<M, CompletionStage<UUID>> results = dispatcher1.dispatchToGroup(new IdentityCommand<>());
				assertThat(results).containsOnlyKeys(group1.getLocalMember());
				assertThat(results.get(group1.getLocalMember()).toCompletableFuture().join()).isSameAs(fooContext1);

				assertThat(dispatcher1.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember()))).isEmpty();

				try (CommandDispatcherFactoryProvider<M> provider2 = this.factory.apply(CLUSTER_NAME, "member2")) {
					CommandDispatcherFactory<M> factory2 = provider2.getCommandDispatcherFactory();
					Group<M> group2 = factory2.getGroup();
					UUID fooContext2 = UUID.randomUUID();

					try (CommandDispatcher<M, UUID> dispatcher2 = factory2.createCommandDispatcher("foo", fooContext2)) {

						assertThat(dispatcher2.getContext()).isSameAs(fooContext2);
						assertThat(dispatcher2.dispatchToMember(new IdentityCommand<>(), group2.getLocalMember()).toCompletableFuture().join()).isSameAs(fooContext2);
						assertThat(dispatcher2.dispatchToMember(new IdentityCommand<>(), group1.getLocalMember()).toCompletableFuture().join()).isEqualTo(fooContext1);

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>());
						assertThat(results).containsOnlyKeys(group1.getLocalMember(), group2.getLocalMember());
						assertThat(results.get(group1.getLocalMember()).toCompletableFuture().join()).isEqualTo(fooContext1);
						assertThat(results.get(group2.getLocalMember()).toCompletableFuture().join()).isSameAs(fooContext2);

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember()));
						assertThat(results).containsOnlyKeys(group2.getLocalMember());
						assertThat(results.get(group2.getLocalMember()).toCompletableFuture().join()).isSameAs(fooContext2);

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group2.getLocalMember()));
						assertThat(results).containsOnlyKeys(group1.getLocalMember());
						assertThat(results.get(group1.getLocalMember()).toCompletableFuture().join()).isEqualTo(fooContext1);

						assertThat(dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember(), group2.getLocalMember()))).isEmpty();
					}

					results = dispatcher1.dispatchToGroup(new IdentityCommand<>());
					assertThat(results).containsKey(group1.getLocalMember());
					assertThat(results.get(group1.getLocalMember()).toCompletableFuture().join()).isSameAs(fooContext1);
					CompletionStage<UUID> result = results.get(group2.getLocalMember());
					if (result != null) {
						assertThatExceptionOfType(CancellationException.class).isThrownBy(result.toCompletableFuture()::join);
					}

					results = dispatcher1.dispatchToGroup(new IdentityCommand<>(), Set.of(group1.getLocalMember()));
					assertThat(results).doesNotContainKey(group1.getLocalMember());
					result = results.get(group2.getLocalMember());
					if (result != null) {
						assertThatExceptionOfType(CancellationException.class).isThrownBy(result.toCompletableFuture()::join);
					}

					assertThatExceptionOfType(CancellationException.class).isThrownBy(dispatcher1.dispatchToMember(new IdentityCommand<>(), group2.getLocalMember()).toCompletableFuture()::join);

					UUID barContext2 = UUID.randomUUID();

					try (CommandDispatcher<M, UUID> dispatcher2 = factory2.createCommandDispatcher("bar", barContext2)) {

						assertThat(dispatcher2.getContext()).isSameAs(barContext2);

						assertThat(dispatcher2.dispatchToMember(new IdentityCommand<>(), group2.getLocalMember()).toCompletableFuture().join()).isSameAs(barContext2);
						assertThatExceptionOfType(CancellationException.class).isThrownBy(dispatcher2.dispatchToMember(new IdentityCommand<>(), group1.getLocalMember()).toCompletableFuture()::join);

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>());
						result = results.get(group1.getLocalMember());
						if (result != null) {
							assertThatExceptionOfType(CancellationException.class).isThrownBy(result.toCompletableFuture()::join);
						}
						assertThat(results.get(group2.getLocalMember()).toCompletableFuture().join()).isSameAs(barContext2);

						results = dispatcher2.dispatchToGroup(new IdentityCommand<>(), Set.of(group2.getLocalMember()));
						result = results.get(group1.getLocalMember());
						if (result != null) {
							assertThatExceptionOfType(CancellationException.class).isThrownBy(result.toCompletableFuture()::join);
						}
						assertThat(results.get(group2.getLocalMember())).isNull();
					}
				}
			}
		}
	}
}
