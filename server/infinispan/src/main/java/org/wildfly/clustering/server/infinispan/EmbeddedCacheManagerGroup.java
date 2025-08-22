/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.infinispan.manager.EmbeddedCacheManager;
import org.wildfly.clustering.server.GroupMembership;
import org.wildfly.clustering.server.GroupMembershipEvent;
import org.wildfly.clustering.server.GroupMembershipListener;
import org.wildfly.clustering.server.GroupMembershipMergeEvent;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * A group composed of embedded cache manager members.
 * @param <A> the group member address type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerGroup<A extends Comparable<A>, M extends GroupMember<A>> implements CacheContainerGroup {

	private final EmbeddedCacheManager manager;
	private final Group<A, M> group;
	private final Function<M, CacheContainerGroupMember> wrapper;
	private final CacheContainerGroupMemberFactory factory;
	private final CacheContainerGroupMember localMember;

	public EmbeddedCacheManagerGroup(EmbeddedCacheManagerGroupConfiguration<A, M> configuration) {
		this.manager = configuration.getCacheContainer();
		this.group = configuration.getGroup();
		this.factory = new EmbeddedCacheManagerGroupMemberFactory(configuration);
		this.wrapper = configuration.getAddressWrapper().<M>compose(GroupMember::getAddress).andThen(this.factory::createGroupMember);
		this.localMember = this.wrapper.apply(this.group.getLocalMember());
	}

	@Override
	public EmbeddedCacheManager getCacheContainer() {
		return this.manager;
	}

	@Override
	public String getName() {
		return this.group.getName();
	}

	@Override
	public CacheContainerGroupMember getLocalMember() {
		return this.localMember;
	}

	@Override
	public GroupMembership<CacheContainerGroupMember> getMembership() {
		return this.wrap(this.group.getMembership());
	}

	@Override
	public boolean isSingleton() {
		return this.group.isSingleton();
	}

	@Override
	public Registration register(GroupMembershipListener<CacheContainerGroupMember> listener) {
		return this.group.register(new GroupMembershipListener<>() {

			@Override
			public void updated(GroupMembershipEvent<M> event) {
				listener.updated(new CacheContainerGroupMembershipEvent(event));
			}

			@Override
			public void split(GroupMembershipEvent<M> event) {
				listener.split(new CacheContainerGroupMembershipEvent(event));
			}

			@Override
			public void merged(GroupMembershipMergeEvent<M> event) {
				listener.merged(new CacheContainerGroupMembershipMergeEvent(event));
			}
		});
	}

	@Override
	public CacheContainerGroupMemberFactory getGroupMemberFactory() {
		return this.factory;
	}

	GroupMembership<CacheContainerGroupMember> wrap(GroupMembership<M> membership) {
		return new CacheGroupMembership<>(membership, this.wrapper);
	}

	static class CacheGroupMembership<A extends Comparable<A>, M extends GroupMember<A>> implements GroupMembership<CacheContainerGroupMember> {
		private final GroupMembership<M> membership;
		private final List<CacheContainerGroupMember> members;
		private final CacheContainerGroupMember coordinator;

		CacheGroupMembership(GroupMembership<M> membership, Function<M, CacheContainerGroupMember> wrapper) {
			this.membership = membership;
			this.members = membership.getMembers().stream().map(wrapper).collect(Collectors.toUnmodifiableList());
			this.coordinator = wrapper.apply(membership.getCoordinator());
		}

		@Override
		public CacheContainerGroupMember getCoordinator() {
			return this.coordinator;
		}

		@Override
		public List<CacheContainerGroupMember> getMembers() {
			return this.members;
		}

		@Override
		public int hashCode() {
			return this.membership.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof CacheGroupMembership membership)) return false;
			return this.membership.equals(membership.membership);
		}

		@Override
		public String toString() {
			return this.membership.toString();
		}
	}

	class CacheContainerGroupMembershipEvent implements GroupMembershipEvent<CacheContainerGroupMember> {
		private final GroupMembership<CacheContainerGroupMember> previousMembership;
		private final GroupMembership<CacheContainerGroupMember> currentMembership;

		CacheContainerGroupMembershipEvent(GroupMembershipEvent<M> event) {
			this.previousMembership = EmbeddedCacheManagerGroup.this.wrap(event.getPreviousMembership());
			this.currentMembership = EmbeddedCacheManagerGroup.this.wrap(event.getCurrentMembership());
		}

		@Override
		public GroupMembership<CacheContainerGroupMember> getPreviousMembership() {
			return this.previousMembership;
		}

		@Override
		public GroupMembership<CacheContainerGroupMember> getCurrentMembership() {
			return this.currentMembership;
		}
	}

	class CacheContainerGroupMembershipMergeEvent extends CacheContainerGroupMembershipEvent implements GroupMembershipMergeEvent<CacheContainerGroupMember> {
		private final List<GroupMembership<CacheContainerGroupMember>> partitions;

		CacheContainerGroupMembershipMergeEvent(GroupMembershipMergeEvent<M> event) {
			super(event);
			this.partitions = event.getPartitions().stream().map(EmbeddedCacheManagerGroup.this::wrap).collect(Collectors.toUnmodifiableList());
		}

		@Override
		public List<GroupMembership<CacheContainerGroupMember>> getPartitions() {
			return this.partitions;
		}
	}
}
