/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * Factory for creating a container-specific session implementation.
 * @param <S> the container-specific session type
 * @param <C> the container-specific session manager context type
 * @author Paul Ferraro
 */
public interface SessionFacadeProvider<S, C> {
	/**
	 * Fabricates a container-specific facade for the specified session and session manager context.
	 * @param session a session
	 * @param context the container-specific session manager context
	 * @return a container-specific session facade
	 */
	S asSession(ImmutableSession session, C context);
}
