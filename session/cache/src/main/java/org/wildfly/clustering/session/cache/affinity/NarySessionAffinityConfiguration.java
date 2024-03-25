/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

/**
 * Configuration of a {@link NarySessionAffinity}.
 * @author Paul Ferraro
 */
public interface NarySessionAffinityConfiguration {

	/**
	 * The delimiter used to join members into a single value.
	 * @return a delimiter
	 */
	String getDelimiter();

	/**
	 * The maximum number of members to include in this affinity.
	 * @return a maximum number of members.
	 */
	default int getMaxMembers() {
		return Integer.MAX_VALUE;
	}
}
