/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import org.jgroups.JChannel;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public interface GroupITCaseConfiguration<A extends Comparable<A>, M extends GroupMember<A>> extends AutoCloseable {

	JChannel getChannel();

	Group<A, M> getGroup();

	String getName();
}
