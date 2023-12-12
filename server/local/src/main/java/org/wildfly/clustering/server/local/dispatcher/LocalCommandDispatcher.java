/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.local.dispatcher;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.server.dispatcher.Command;
import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.local.LocalGroupMember;

/**
 * Non-clustered {@link CommandDispatcher} implementation
 * @author Paul Ferraro
 * @param <C> command context
 */
public class LocalCommandDispatcher<C> implements CommandDispatcher<LocalGroupMember, C> {

	private final LocalGroupMember member;
	private final C context;
	private volatile boolean closed = false;

	public LocalCommandDispatcher(LocalGroupMember member, C context) {
		this.member = member;
		this.context = context;
	}

	@Override
	public C getContext() {
		return this.context;
	}

	@Override
	public <R, E extends Exception> CompletionStage<R> dispatchToMember(Command<R, ? super C, E> command, LocalGroupMember member) {
		if (this.closed) {
			throw new IllegalStateException();
		}
		if (!this.member.equals(member)) {
			throw new IllegalArgumentException(member.toString());
		}
		try {
			return CompletableFuture.completedFuture(command.execute(this.context));
		} catch (Exception e) {
			return CompletableFuture.failedStage(e);
		}
	}

	@Override
	public <R, E extends Exception> Map<LocalGroupMember, CompletionStage<R>> dispatchToGroup(Command<R, ? super C, E> command, Set<LocalGroupMember> excluding) {
		if (this.closed) {
			throw new IllegalStateException();
		}
		return excluding.contains(this.member) ? Map.of() : Map.of(this.member, this.dispatchToMember(command, this.member));
	}

	@Override
	public void close() {
		this.closed = true;
	}
}
