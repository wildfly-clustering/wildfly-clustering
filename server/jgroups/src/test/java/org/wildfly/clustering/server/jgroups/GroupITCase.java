/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.protocols.DISCARD;
import org.jgroups.protocols.TP;
import org.jgroups.stack.ProtocolStack;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.server.GroupMembership;
import org.wildfly.clustering.server.GroupMembershipEvent;
import org.wildfly.clustering.server.GroupMembershipListener;
import org.wildfly.clustering.server.GroupMembershipMergeEvent;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * Base integration test for {@link Group} implementations.
 * @param <A> the address type of the group member
 * @param <M> the group member type
 * @param <G> the group type
 * @author Paul Ferraro
 */
public abstract class GroupITCase<A extends Comparable<A>, M extends GroupMember<A>, G extends Group<A, M>> {
	private static final String CLUSTER_NAME = "cluster";
	private static final String[] MEMBER_NAMES = new String[] { "member0", "member1", "member2" };
	private static final Duration VIEW_CHANGE_DURATION = Duration.ofSeconds(20);
	private static final Duration SPLIT_MERGE_DURATION = Duration.ofSeconds(120);

	private final System.Logger logger = System.getLogger(this.getClass().getName());
	private final Function<JChannel, Context<G>> groupContextFactory;
	private final Function<A, Address> mapper;

	protected GroupITCase(Function<JChannel, Context<G>> groupContextFactory, Function<A, Address> mapper) {
		this.groupContextFactory = groupContextFactory;
		this.mapper = mapper;
	}

	@Test
	public void test() throws Exception {
		try (Context<JChannel> channel1Context = new JChannelContext(CLUSTER_NAME, MEMBER_NAMES[0])) {
			JChannel channel1 = channel1Context.get();
			try (Context<G> groupContext1 = this.groupContextFactory.apply(channel1)) {
				Group<A, M> group1 = groupContext1.get();

				assertThat(group1.getName()).isSameAs(channel1.getClusterName());
				assertThat(group1.getLocalMember().getName()).isEqualTo(MEMBER_NAMES[0]);
				assertThat(group1.isSingleton()).isFalse();
				this.validate(channel1, group1);

				GroupMembership<M> previousMembership = null;
				GroupMembership<M> currentMembership = group1.getMembership();

				assertThat(currentMembership.getCoordinator()).isEqualTo(group1.getLocalMember());
				assertThat(currentMembership.getMembers()).containsExactly(group1.getLocalMember());

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

					assertThat(updateEvent).isNull();
					assertThat(splitEvent).isNull();
					assertThat(mergeEvent).isNull();

					try (Context<JChannel> channel2Context = new JChannelContext(CLUSTER_NAME, MEMBER_NAMES[1])) {
						JChannel channel2 = channel2Context.get();

						// Verify cluster formation
						assertThat(channel2.getView().getMembers()).containsExactly(channel1.getAddress(), channel2.getAddress());

						Instant start = Instant.now();
						updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
						this.logger.log(System.Logger.Level.INFO, "View change detected after {0}", Duration.between(start, Instant.now()));
						splitEvent = splitEvents.poll();
						mergeEvent = mergeEvents.poll();

						assertThat(updateEvent).isNotNull();
						assertThat(splitEvent).isNull();
						assertThat(mergeEvent).isNull();

						previousMembership = currentMembership;
						currentMembership = group1.getMembership();

						assertThat(updateEvent.getPreviousMembership()).isEqualTo(previousMembership);
						assertThat(updateEvent.getCurrentMembership()).isEqualTo(currentMembership);

						try (Context<G> group2Context = this.groupContextFactory.apply(channel2)) {
							Group<A, M> group2 = group2Context.get();

							assertThat(group2.getName()).isSameAs(channel2.getClusterName());
							assertThat(group2.getLocalMember().getName()).isEqualTo(MEMBER_NAMES[1]);
							assertThat(group2.isSingleton()).isFalse();

							this.validate(channel1, group1);
							this.validate(channel2, group2);

							GroupMembership<M> membership2 = group2.getMembership();

							assertThat(membership2).isEqualTo(currentMembership);
							assertThat(membership2.getCoordinator()).isEqualTo(currentMembership.getCoordinator());
							assertThat(membership2.getMembers()).containsExactlyElementsOf(currentMembership.getMembers());

							try (Context<JChannel> channelContext3 = new JChannelContext(CLUSTER_NAME, MEMBER_NAMES[2])) {
								JChannel channel3 = channelContext3.get();

								// Verify cluster formation
								assertThat(channel3.getView().getMembers()).containsExactly(channel1.getAddress(), channel2.getAddress(), channel3.getAddress());

								start = Instant.now();
								updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
								this.logger.log(System.Logger.Level.INFO, "View change detected after {0}", Duration.between(start, Instant.now()));
								splitEvent = splitEvents.poll();
								mergeEvent = mergeEvents.poll();

								assertThat(updateEvent).isNotNull();
								assertThat(splitEvent).isNull();
								assertThat(mergeEvent).isNull();

								previousMembership = currentMembership;
								currentMembership = group1.getMembership();

								assertThat(updateEvent.getPreviousMembership()).isEqualTo(previousMembership);
								assertThat(updateEvent.getCurrentMembership()).isEqualTo(currentMembership);

								try (Context<G> group3Context = this.groupContextFactory.apply(channel3)) {
									Group<A, M> group3 = group3Context.get();

									assertThat(group3.getName()).isSameAs(channel3.getClusterName());
									assertThat(group3.getLocalMember().getName()).isEqualTo(MEMBER_NAMES[2]);
									assertThat(group3.isSingleton()).isFalse();

									this.validate(channel1, group1);
									this.validate(channel2, group2);
									this.validate(channel3, group3);

									GroupMembership<M> membership3 = group3.getMembership();

									assertThat(membership3).isEqualTo(currentMembership);
									assertThat(membership3.getCoordinator()).isEqualTo(currentMembership.getCoordinator());
									assertThat(membership3.getMembers()).containsExactlyElementsOf(currentMembership.getMembers());

									this.logger.log(System.Logger.Level.INFO, "Simulating network partition");
									DISCARD discard1 = new DISCARD().discardAll(true);
									channel1.getProtocolStack().insertProtocol(discard1, ProtocolStack.Position.ABOVE, TP.class);

									start = Instant.now();
									splitEvent = splitEvents.poll(SPLIT_MERGE_DURATION.toSeconds(), TimeUnit.SECONDS);
									this.logger.log(System.Logger.Level.INFO, "Network partition created after {0}", Duration.between(start, Instant.now()));
									updateEvent = updateEvents.poll();
									mergeEvent = mergeEvents.poll();

									assertThat(updateEvent).isNull();
									assertThat(splitEvent).isNotNull();
									assertThat(mergeEvent).isNull();

									this.validate(channel1, group1);
									this.validate(channel2, group2);
									this.validate(channel3, group3);

									previousMembership = currentMembership;
									currentMembership = group1.getMembership();

									assertThat(splitEvent.getPreviousMembership()).isEqualTo(previousMembership);
									assertThat(splitEvent.getCurrentMembership()).isEqualTo(currentMembership);
									assertThat(currentMembership.getMembers()).containsExactly(group1.getLocalMember());
									assertThat(currentMembership.getCoordinator()).isEqualTo(group1.getLocalMember());

									this.logger.log(System.Logger.Level.INFO, "Resolving network partition");
									channel1.getProtocolStack().removeProtocol(DISCARD.class);

									start = Instant.now();
									mergeEvent = mergeEvents.poll(SPLIT_MERGE_DURATION.toSeconds(), TimeUnit.SECONDS);
									this.logger.log(System.Logger.Level.INFO, "Network partition resolved after {0}", Duration.between(start, Instant.now()));
									splitEvent = splitEvents.poll();
									updateEvent = updateEvents.poll();

									assertThat(updateEvent).isNull();
									assertThat(splitEvent).isNull();
									assertThat(mergeEvent).isNotNull();

									this.validate(channel1, group1);
									this.validate(channel2, group2);
									this.validate(channel3, group3);

									previousMembership = currentMembership;
									currentMembership = group1.getMembership();

									assertThat(mergeEvent.getPreviousMembership()).isEqualTo(previousMembership);
									assertThat(mergeEvent.getCurrentMembership()).isEqualTo(currentMembership);
									assertThat(currentMembership.getMembers()).containsExactlyInAnyOrder(group1.getLocalMember(), group2.getLocalMember(), group3.getLocalMember());
									assertThat(mergeEvent.getPartitions()).hasSize(2).contains(previousMembership);
								} // Close group3
							} // Close channel3

							start = Instant.now();
							updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
							this.logger.log(System.Logger.Level.INFO, "View change detected after {0}", Duration.between(start, Instant.now()));
							splitEvent = splitEvents.poll();
							mergeEvent = mergeEvents.poll();

							assertThat(updateEvent).isNotNull();
							assertThat(splitEvent).isNull();
							assertThat(mergeEvent).isNull();

							this.validate(channel1, group1);
							this.validate(channel2, group2);

							previousMembership = currentMembership;
							currentMembership = group1.getMembership();

							assertThat(updateEvent.getPreviousMembership()).isEqualTo(previousMembership);
							assertThat(updateEvent.getCurrentMembership()).isEqualTo(currentMembership);
							assertThat(currentMembership.getMembers()).containsExactlyInAnyOrder(group1.getLocalMember(), group2.getLocalMember());
						} // Close group2
					} // Close channel2

					Instant start = Instant.now();
					updateEvent = updateEvents.poll(VIEW_CHANGE_DURATION.toSeconds(), TimeUnit.SECONDS);
					this.logger.log(System.Logger.Level.INFO, "View change detected after {0}", Duration.between(start, Instant.now()));
					splitEvent = splitEvents.poll();
					mergeEvent = mergeEvents.poll();

					assertThat(updateEvent).isNotNull();
					assertThat(splitEvent).isNull();
					assertThat(mergeEvent).isNull();

					this.validate(channel1, group1);

					previousMembership = currentMembership;
					currentMembership = group1.getMembership();

					assertThat(updateEvent.getPreviousMembership()).isEqualTo(previousMembership);
					assertThat(updateEvent.getCurrentMembership()).isEqualTo(currentMembership);
					assertThat(currentMembership.getCoordinator()).isEqualTo(group1.getLocalMember());
					assertThat(currentMembership.getMembers()).containsExactly(group1.getLocalMember());
				} // Close group1
			} // Close channel1
		}
	}

	private void validate(JChannel channel, Group<A, M> group) {

		assertThat(group.getLocalMember().getName()).isEqualTo(channel.getName());
		assertThat(this.mapper.apply(group.getLocalMember().getAddress())).isEqualTo(channel.getAddress());

		View view = channel.getView();
		GroupMembership<M> membership = group.getMembership();

		assertThat(this.mapper.apply(membership.getCoordinator().getAddress())).isEqualTo(view.getCoord());
		assertThat(membership.getMembers().stream().map(GroupMember::getAddress).map(this.mapper).toList()).containsExactlyElementsOf(view.getMembers());
	}
}
