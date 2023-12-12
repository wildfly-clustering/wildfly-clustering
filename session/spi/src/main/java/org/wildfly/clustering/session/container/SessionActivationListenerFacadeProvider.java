/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides container-specific activation/passivation listener mechanics.
 * @param <S> the container-specific session facade type
 * @param <C> the container-specific session manager context type
 * @param <L> the container-specific activation listener type
 * @author Paul Ferraro
 */
public interface SessionActivationListenerFacadeProvider<S, C, L> extends SessionFacadeProvider<S, C> {
	/**
	 * Fabricates an optional container-specific listener for the specified attribute.
	 * @return an optional container-specific listener.
	 */
	default Optional<L> asSessionActivationListener(Object attribute) {
		return Optional.empty();
	}

	/**
	 * Returns a pre-passivate notifier for the specified container-specific listener.
	 * @param listener the specification listener
	 * @return a consumer for a session
	 */
	Consumer<S> prePassivateNotifier(L listener);

	/**
	 * Returns a post-activate notifier for the specified container-specific listener.
	 * @param listener the specification listener
	 * @return a consumer for a session
	 */
	Consumer<S> postActivateNotifier(L listener);

	/**
	 * Fabricates a container-specific listener with the specified pre-passivate and post-activate logic.
	 * @param prePassivate a pre-passivate event consumer for a session
	 * @param postActivate a post-activate event consumer for a session
	 * @return a specification listener implementation
	 */
	L asSessionActivationListener(Consumer<S> prePassivate, Consumer<S> postActivate);
}
