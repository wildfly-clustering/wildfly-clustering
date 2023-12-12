/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import org.wildfly.clustering.server.group.GroupMember;

/**
 * Local view of a group member.
 * @author Paul Ferraro
 */
public interface LocalGroupMember extends GroupMember<String> {

	@Override
	default String getAddress() {
		return this.getName();
	}

	static LocalGroupMember of(String memberName) {
		return new DefaultLocalGroupMember(memberName);
	}
}
