/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import java.util.function.Function;

import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerGroup;

/**
 * A factory for creating a {@link CommandDispatcher} for dispatching commands to embedded cache manager group members.
 * @param <A> the address type for group members
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerCommandDispatcherFactory<A extends Comparable<A>, M extends GroupMember<A>> implements CacheContainerCommandDispatcherFactory {

	private final GroupCommandDispatcherFactory<A, M> dispatcherFactory;
	private final EmbeddedCacheManagerGroup<A, M> group;
	private final Function<M, CacheContainerGroupMember> wrapper;
	private final Function<CacheContainerGroupMember, M> unwrapper;

	public EmbeddedCacheManagerCommandDispatcherFactory(EmbeddedCacheManagerCommandDispatcherFactoryConfiguration<A, M> configuration) {
		this.dispatcherFactory = configuration.getCommandDispatcherFactory();
		this.group = new EmbeddedCacheManagerGroup<>(configuration);
		this.wrapper = configuration.getAddressWrapper().<M>compose(GroupMember::getId).andThen(this.group.getGroupMemberFactory()::createGroupMember);
		this.unwrapper = configuration.getAddressUnwrapper().compose(CacheContainerGroupMember::getId).andThen(configuration.getGroup().getGroupMemberFactory()::createGroupMember);
	}

	@Override
	public CacheContainerGroup getGroup() {
		return this.group;
	}

	@Override
	public <C> CommandDispatcher<CacheContainerGroupMember, C> createCommandDispatcher(Object id, C context, ClassLoader loader) {
		return new EmbeddedCacheManagerCommandDispatcher<>(this.dispatcherFactory.createCommandDispatcher(id, context, loader), this.unwrapper, this.wrapper);
	}
}
