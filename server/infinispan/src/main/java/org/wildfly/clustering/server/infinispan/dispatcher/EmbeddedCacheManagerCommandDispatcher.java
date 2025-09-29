/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.wildfly.clustering.server.dispatcher.Command;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;

/**
 * A {@link CommandDispatcher} for dispatching commands to embedded cache manager group members.
 * @param <A> the address type for group members
 * @param <M> the group member type
 * @param <C> the command context type
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerCommandDispatcher<A extends Comparable<A>, M extends GroupMember<A>, C> implements CommandDispatcher<CacheContainerGroupMember, C> {
	private final CommandDispatcher<M, C> dispatcher;
	private final Function<CacheContainerGroupMember, M> unwrapper;
	private final Function<M, CacheContainerGroupMember> wrapper;

	/**
	 * Creates a command dispatcher decorator.
	 * @param dispatcher the decorated command dispatcher
	 * @param unwrapper a function that converts a cache container group member to the member type expected by the decorated command dispatcher
	 * @param wrapper a function that converts a cache container group member from the member type expected by the decorated command dispatcher
	 */
	public EmbeddedCacheManagerCommandDispatcher(CommandDispatcher<M, C> dispatcher, Function<CacheContainerGroupMember, M> unwrapper, Function<M, CacheContainerGroupMember> wrapper) {
		this.dispatcher = dispatcher;
		this.wrapper = wrapper;
		this.unwrapper = unwrapper;
	}

	@Override
	public C getContext() {
		return this.dispatcher.getContext();
	}

	@Override
	public <R, E extends Exception> CompletionStage<R> dispatchToMember(Command<R, ? super C, E> command, CacheContainerGroupMember member) throws IOException {
		return this.dispatcher.dispatchToMember(command, this.unwrapper.apply(member));
	}

	@Override
	public <R, E extends Exception> Map<CacheContainerGroupMember, CompletionStage<R>> dispatchToGroup(Command<R, ? super C, E> command) throws IOException {
		return map(this.dispatcher.dispatchToGroup(command));
	}

	@Override
	public <R, E extends Exception> Map<CacheContainerGroupMember, CompletionStage<R>> dispatchToGroup(Command<R, ? super C, E> command, Set<CacheContainerGroupMember> excluding) throws IOException {
		return map(this.dispatcher.dispatchToGroup(command, excluding.stream().map(this.unwrapper).collect(Collectors.toSet())));
	}

	private <R> Map<CacheContainerGroupMember, CompletionStage<R>> map(Map<M, CompletionStage<R>> map) {
		return map.entrySet().stream().collect(Collectors.toMap(this.wrapper.compose(Map.Entry::getKey), Map.Entry::getValue));
	}

	@Override
	public void close() {
		this.dispatcher.close();
	}
}
