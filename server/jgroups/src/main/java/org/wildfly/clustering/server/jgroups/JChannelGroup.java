/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.wildfly.clustering.server.GroupMembershipEvent;
import org.wildfly.clustering.server.GroupMembershipListener;
import org.wildfly.clustering.server.GroupMembershipMergeEvent;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.GroupMembership;
import org.wildfly.clustering.server.listener.ListenerRegistrar;
import org.wildfly.clustering.server.local.listener.LocalListenerRegistrar;

/**
 * @author Paul Ferraro
 */
public class JChannelGroup implements ChannelGroup, Receiver {
	private static final System.Logger LOGGER = System.getLogger(JChannelGroup.class.getName());

	private final String name;
	private final ChannelGroupMemberFactory memberFactory = JChannelGroupMember::new;
	private final ChannelGroupMember localMember;
	private final ListenerRegistrar<GroupMembershipListener<ChannelGroupMember>> listeners;
	private final AtomicReference<View> view = new AtomicReference<>();

	public JChannelGroup(JChannel channel) {
		this.name = channel.getClusterName();
		this.localMember = this.memberFactory.createGroupMember(channel.getAddress());
		this.listeners = new LocalListenerRegistrar<>(Duration.ofMillis(channel.getProtocolStack().getTransport().getWhoHasCacheTimeout()));
		channel.setReceiver(this);
		this.view.compareAndSet(null, channel.getView());
	}

	@Override
	public Registration register(GroupMembershipListener<ChannelGroupMember> listener) {
		return this.listeners.register(listener);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public GroupMembership<ChannelGroupMember> getMembership() {
		return new ViewMembership(this.view.get(), this.memberFactory);
	}

	@Override
	public ChannelGroupMember getLocalMember() {
		return this.localMember;
	}

	@Override
	public ChannelGroupMemberFactory getGroupMemberFactory() {
		return this.memberFactory;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public void viewAccepted(View view) {
		try {
			View previousView = this.view.getAndSet(view);
			if (previousView != null) {
				if (view instanceof MergeView) {
					GroupMembershipMergeEvent<ChannelGroupMember> event = new DefaultGroupMembershipMergeEvent(previousView, (MergeView) view);
					this.listeners.accept(listener -> listener.merged(event));
				} else {
					List<Address> leavers = View.leftMembers(previousView, view);
					GroupMembershipEvent<ChannelGroupMember> event = new DefaultGroupMembershipEvent(previousView, view);
					this.listeners.accept(listener -> {
						// TODO Update logic to consider *abrupt* leavers only
						if (leavers.size() > 1) {
							listener.split(event);
						} else {
							listener.updated(event);
						}
					});
				}
			}
		} catch (Throwable e) {
			LOGGER.log(System.Logger.Level.ERROR, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void close() {
		this.listeners.close();
	}

	GroupMembership<ChannelGroupMember> membership(View view) {
		return new ViewMembership(view, this.memberFactory);
	}

	private class DefaultGroupMembershipEvent implements GroupMembershipEvent<ChannelGroupMember> {
		private final GroupMembership<ChannelGroupMember> previousMembership;
		private final GroupMembership<ChannelGroupMember> currentMembership;

		DefaultGroupMembershipEvent(View previousView, View currentView) {
			this.previousMembership = JChannelGroup.this.membership(previousView);
			this.currentMembership = JChannelGroup.this.membership(currentView);
		}

		@Override
		public GroupMembership<ChannelGroupMember> getPreviousMembership() {
			return this.previousMembership;
		}

		@Override
		public GroupMembership<ChannelGroupMember> getCurrentMembership() {
			return this.currentMembership;
		}
	}

	private class DefaultGroupMembershipMergeEvent extends DefaultGroupMembershipEvent implements GroupMembershipMergeEvent<ChannelGroupMember> {
		private final List<org.wildfly.clustering.server.GroupMembership<ChannelGroupMember>> partitions;

		DefaultGroupMembershipMergeEvent(View previousView, MergeView currentView) {
			super(previousView, currentView);
			this.partitions = currentView.getSubgroups().stream().map(JChannelGroup.this::membership).collect(Collectors.toUnmodifiableList());
		}

		@Override
		public List<org.wildfly.clustering.server.GroupMembership<ChannelGroupMember>> getPartitions() {
			return this.partitions;
		}
	}
}
