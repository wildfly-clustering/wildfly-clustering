/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * Provides specification facades to a session manager implementation.
 * @author Paul Ferraro
 * @param <S> the specification type for a session
 * @param <C> the specification type for a deployment context
 * @author Paul Ferraro
 * @deprecated Superseded by {@link org.wildfly.clustering.session.container.ContainerProvider}.
 */
@Deprecated(forRemoval = true)
public interface SessionSpecificationProvider<S, C> {

	/**
	 * Fabricates a read-only specification facade for the specified session and session manager context.
	 * @param session a session
	 * @param context the container-specific session manager context
	 * @return a container-specific session facade
	 */
	S asSession(ImmutableSession session, C context);
}
