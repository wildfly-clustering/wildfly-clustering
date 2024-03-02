/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec;

import java.util.Optional;
import java.util.function.Consumer;

import org.wildfly.clustering.session.ImmutableSession;

/**
 * Provides specification facades to a session manager implementation.
 * @author Paul Ferraro
 * @param <S> the specification type for a session
 * @param <C> the specification type for a deployment context
 * @param <L> the specification type for a session activation listener
 * @author Paul Ferraro
 */
public interface SessionSpecificationProvider<S, C, L> {
	/**
	 * Fabricates a specification facade for the specified session and session manager context.
	 * @param session a session
	 * @param context the container-specific session manager context
	 * @return a container-specific session facade
	 */
	S asSession(ImmutableSession session, C context);

	/**
	 * Fabricates an optional container-specific listener for the specified attribute.
	 * @return an optional container-specific listener.
	 */
	default Optional<L> asSessionActivationListener(Object attribute) {
		Class<L> listenerClass = this.getSessionActivationListenerClass();
		return Optional.ofNullable(attribute).filter(listenerClass::isInstance).map(listenerClass::cast);
	}

	/**
	 * Returns the specification type of a session activation listener.
	 * @return
	 */
	Class<L> getSessionActivationListenerClass();

	/**
	 * Returns a pre-passivate notifier for the specified session activation specification listener.
	 * @param listener the specification listener
	 * @return a consumer for a session
	 */
	Consumer<S> prePassivate(L listener);

	/**
	 * Returns a post-activate notifier for the specified session activation specification listener.
	 * @param listener the specification listener
	 * @return a consumer for a session
	 */
	Consumer<S> postActivate(L listener);

	/**
	 * Fabricates a specification listener with the specified pre-passivate and post-activate logic.
	 * @param prePassivate a pre-passivate event consumer for a session
	 * @param postActivate a post-activate event consumer for a session
	 * @return a specification listener implementation
	 */
	L asSessionActivationListener(Consumer<S> prePassivate, Consumer<S> postActivate);
}
