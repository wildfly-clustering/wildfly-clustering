/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.protocols.DISCARD;
import org.jgroups.protocols.TP;
import org.jgroups.stack.ProtocolStack;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.GroupMembership;
import org.wildfly.clustering.server.GroupMembershipEvent;
import org.wildfly.clustering.server.GroupMembershipListener;
import org.wildfly.clustering.server.GroupMembershipMergeEvent;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.common.function.ExceptionBiFunction;

/**
 * @author Paul Ferraro
 */
public abstract class GroupITCase<A extends Comparable<A>, M extends GroupMember<A>> {
	private static final String CLUSTER_NAME = "cluster";
	private static final String[] MEMBER_NAMES = new String[] { "member0", "member1", "member2" };
	private static final Duration VIEW_CHANGE_DURATION = Duration.ofSeconds(20);
	private static final Duration SPLIT_MERGE_DURATION = Duration.ofSeconds(120);

	private final ExceptionBiFunction<String, String, GroupITCaseConfiguration<A, M>, Exception> factory;
	private final Function<A, Address> mapper;

	protected GroupITCase(ExceptionBiFunction<String, String, GroupITCaseConfiguration<A, M>, Exception> factory, Function<A, Address> mapper) {
		this.factory = factory;
		this.mapper = mapper;
	}

	@Test
	public void test() throws Exception {
		try (GroupITCaseConfiguration<A, M> config1 = this.factory.apply(CLUSTER_NAME, MEMBER_NAMES[0])) {
			Group<A, M> group1 = config1.getGroup();
			JChannel channel1 = config1.getChannel();

			assertSame(config1.getName(), group1.getName());
			assertEquals(MEMBER_NAMES[0], group1.getLocalMember().getName());
			assertFalse(group1.isSingleton());
			this.validate(channel1, group1);

			GroupMembership<M> previousMembership = null;
			GroupMembership<M> currentMembership = group1.getMembership();

			assertEquals(group1.getLocalMember(), currentMembership.getCoordinator());
			assertEquals(List.of(group1.getLocalMember()), currentMembership.getMembers());

			BlockingQueue<GroupMembershipEvent<M>> updateEvents = new LinkedBlockingQueue<>();
			BlockingQueue<GroupMembershipEvent<M>> splitEvents = new LinkedBlockingQueue<>();
			BlockingQueue<GroupMembershipMergeEvent<M>> mergeEvents = new LinkedBlockingQueue<>();

			GroupMembershipListener<M> listener = new GroupMembershipListener<>() {
				@Override
				public void updated(GroupMembershipEvent<M> event) {
					updateEvents.add(event);
				}

				@Override
				public void split(GroupMembershipEvent<M> event) {
					splitEvents.add(event);
				}

				@Override
				public void merged(GroupMembershipMergeEvent<M> event) {
					mergeEvents.add(event);
				}
			};
			try (Registration registration = group1.register(listener)) {

				GroupMembershipEvent<M> updateEvent = updateEvents.poll();
				GroupMembershipEvent<M> splitEvent = splitEvents.poll();
				GroupMembershipMergeEvent<M> mergeEvent = mergeEvents.poll();

				assertNull(updateEvent);
				assertNull(splitEvent);
				assertNull(mergeEvent);

				try (GroupITCaseConfiguration<A, M> config2 = this.factory.apply(CLUSTER_NAME, MEMBER_NAMES[1])) {
					JChannel channel2 = config2.getChannel();

					// Verify cluster formation
					assertEquals(channel1.getView(), channel2.getView());
					assertEquals(2, channel1.getView().size());

					Instant start = Instant.now();
					updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
					System.out.println("View change detected after " + Duration.between(start, Instant.now()));
					splitEvent = splitEvents.poll();
					mergeEvent = mergeEvents.poll();

					assertNull(splitEvent);
					assertNull(mergeEvent);
					assertNotNull(updateEvent);

					previousMembership = currentMembership;
					currentMembership = group1.getMembership();

					assertEquals(previousMembership, updateEvent.getPreviousMembership());
					assertEquals(currentMembership, updateEvent.getCurrentMembership());

					Group<A, M> group2 = config2.getGroup();

					assertSame(config2.getName(), group2.getName());
					assertEquals(MEMBER_NAMES[1], group2.getLocalMember().getName());
					assertFalse(group2.isSingleton());

					this.validate(channel1, group1);
					this.validate(channel2, group2);

					GroupMembership<M> membership2 = group2.getMembership();

					assertEquals(currentMembership, membership2);
					assertEquals(currentMembership.getCoordinator(), membership2.getCoordinator());
					assertEquals(currentMembership.getMembers(), membership2.getMembers());

					try (GroupITCaseConfiguration<A, M> config3 = this.factory.apply(CLUSTER_NAME, MEMBER_NAMES[2])) {
						JChannel channel3 = config3.getChannel();

						// Verify cluster formation
						assertEquals(channel1.getView(), channel3.getView());
						assertEquals(3, channel1.getView().size());

						start = Instant.now();
						updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
						System.out.println("View change detected after " + Duration.between(start, Instant.now()));
						splitEvent = splitEvents.poll();
						mergeEvent = mergeEvents.poll();

						assertNull(mergeEvent);
						assertNull(splitEvent);
						assertNotNull(updateEvent);

						previousMembership = currentMembership;
						currentMembership = group1.getMembership();

						assertEquals(previousMembership, updateEvent.getPreviousMembership());
						assertEquals(currentMembership, updateEvent.getCurrentMembership());

						Group<A, M> group3 = config3.getGroup();

						assertSame(config3.getName(), group3.getName());
						assertEquals(MEMBER_NAMES[2], group3.getLocalMember().getName());
						assertFalse(group3.isSingleton());

						this.validate(channel1, group1);
						this.validate(channel2, group2);
						this.validate(channel3, group3);

						GroupMembership<M> membership3 = group3.getMembership();

						assertEquals(currentMembership, membership3);
						assertEquals(currentMembership.getCoordinator(), membership3.getCoordinator());
						assertEquals(currentMembership.getMembers(), membership3.getMembers());

						System.out.println("Simulating network partition");
						DISCARD discard1 = new DISCARD().discardAll(true);
						channel1.getProtocolStack().insertProtocol(discard1, ProtocolStack.Position.ABOVE, TP.class);

						start = Instant.now();
						splitEvent = splitEvents.poll(SPLIT_MERGE_DURATION.toSeconds(), TimeUnit.SECONDS);
						System.out.println("Network partition created after " + Duration.between(start, Instant.now()));
						updateEvent = updateEvents.poll();
						mergeEvent = mergeEvents.poll();

						assertNull(mergeEvent);
						assertNull(updateEvent);
						assertNotNull(splitEvent);

						this.validate(channel1, group1);
						this.validate(channel2, group2);
						this.validate(channel3, group3);

						previousMembership = currentMembership;
						currentMembership = group1.getMembership();

						assertEquals(previousMembership, splitEvent.getPreviousMembership());
						assertEquals(currentMembership, splitEvent.getCurrentMembership());
						assertEquals(1, currentMembership.getMembers().size());

						assertEquals(group1.getLocalMember(), currentMembership.getCoordinator());
						assertEquals(List.of(group1.getLocalMember()), currentMembership.getMembers());

						System.out.println("Resolving network partition");
						channel1.getProtocolStack().removeProtocol(DISCARD.class);

						start = Instant.now();
						mergeEvent = mergeEvents.poll(SPLIT_MERGE_DURATION.toSeconds(), TimeUnit.SECONDS);
						System.out.println("Network partition resolved after " + Duration.between(start, Instant.now()));
						splitEvent = splitEvents.poll();
						updateEvent = updateEvents.poll();

						assertNull(updateEvent);
						assertNull(splitEvent);
						assertNotNull(mergeEvent);

						this.validate(channel1, group1);
						this.validate(channel2, group2);
						this.validate(channel3, group3);

						previousMembership = currentMembership;
						currentMembership = group1.getMembership();

						assertEquals(previousMembership, mergeEvent.getPreviousMembership());
						assertEquals(currentMembership, mergeEvent.getCurrentMembership());
						assertEquals(3, currentMembership.getMembers().size());
						assertEquals(2, mergeEvent.getPartitions().size());
						assertTrue(mergeEvent.getPartitions().contains(previousMembership));
					}

					start = Instant.now();
					updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
					System.out.println("View change detected after " + Duration.between(start, Instant.now()));
					splitEvent = splitEvents.poll();
					mergeEvent = mergeEvents.poll();

					assertNull(mergeEvent);
					assertNull(splitEvent);
					assertNotNull(updateEvent);

					this.validate(channel1, group1);
					this.validate(channel2, group2);

					previousMembership = currentMembership;
					currentMembership = group1.getMembership();

					assertEquals(previousMembership, updateEvent.getPreviousMembership());
					assertEquals(currentMembership, updateEvent.getCurrentMembership());

					assertEquals(2, currentMembership.getMembers().size());
				}

				Instant start = Instant.now();
				updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
				System.out.println("View change detected after " + Duration.between(start, Instant.now()));
				splitEvent = splitEvents.poll();
				mergeEvent = mergeEvents.poll();

				assertNull(splitEvent);
				assertNull(mergeEvent);
				assertNotNull(updateEvent);

				this.validate(channel1, group1);

				previousMembership = currentMembership;
				currentMembership = group1.getMembership();

				assertEquals(previousMembership, updateEvent.getPreviousMembership());
				assertEquals(currentMembership, updateEvent.getCurrentMembership());

				assertEquals(group1.getLocalMember(), currentMembership.getCoordinator());
				assertEquals(1, currentMembership.getMembers().size());
				assertEquals(List.of(group1.getLocalMember()), currentMembership.getMembers());
			}
		}
	}

	private void validate(JChannel channel, Group<A, M> group) {

		assertEquals(channel.getName(), group.getLocalMember().getName());
		assertEquals(channel.getAddress(), this.mapper.apply(group.getLocalMember().getAddress()));

		View view = channel.getView();
		GroupMembership<M> membership = group.getMembership();

		assertEquals(view.getCoord(), this.mapper.apply(membership.getCoordinator().getAddress()));
		assertEquals(view.getMembers(), membership.getMembers().stream().map(GroupMember::getAddress).map(this.mapper).collect(Collectors.toUnmodifiableList()));
	}
}
