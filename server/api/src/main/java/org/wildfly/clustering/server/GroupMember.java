/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server;

/**
 * Encapsulates a group member.
 * @author Paul Ferraro
 */
public interface GroupMember {

	/**
	 * Returns the logical name of this group member.
	 * @return the logical name of this group member.
	 */
	String getName();
}
