/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.util.Set;

/**
 * Encapsulates the statistics provided by a session manager.
 * @author Paul Ferraro
 */
public interface SessionStatistics {

	/**
	 * Returns the identifiers of active sessions managed by this session manager.
	 * @return a set of session identifiers.
	 */
	Set<String> getActiveSessions();

	/**
	 * Returns the identifiers of all sessions managed by this session manager, including passive sessions.
	 * @return a set of session identifiers.
	 */
	Set<String> getSessions();

	/**
	 * Returns the number of active sessions.
	 * @return the number of active sessions.
	 */
	default long getActiveSessionCount() {
		return this.getActiveSessions().size();
	}
}
