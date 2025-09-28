/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import org.wildfly.clustering.server.group.GroupMemberFactory;

/**
 * Factory for creating local group members.
 * @author Paul Ferraro
 */
public interface LocalGroupMemberFactory extends GroupMemberFactory<String, LocalGroupMember> {

}
