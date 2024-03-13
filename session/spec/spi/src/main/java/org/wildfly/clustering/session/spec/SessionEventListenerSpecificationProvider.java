/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.spec;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides event specification facades to a session manager implementation.
 * @author Paul Ferraro
 * @param <S> the specification type for a session
 * @param <L> the specification type for a session passivation listener
 */
public interface SessionEventListenerSpecificationProvider<S, L> {

	/**
	 * Fabricates an optional container-specific listener for the specified attribute.
	 * @param attribute the session attribute event source
	 * @return an optional container-specific listener.
	 */
	default Optional<L> asEventListener(Object attribute) {
		Class<L> listenerClass = this.getEventListenerClass();
		return Optional.ofNullable(attribute).filter(listenerClass::isInstance).map(listenerClass::cast);
	}

	/**
	 * Returns the specification type of the session event listener.
	 * @return the specification type of the session event listener.
	 */
	Class<L> getEventListenerClass();

	/**
	 * Returns a pre-event notifier for the specified session event listener.
	 * @param listener the specification listener
	 * @return a consumer for a session
	 */
	Consumer<S> preEvent(L listener);

	/**
	 * Returns a post-event notifier for the specified session event listener.
	 * @param listener the specification listener
	 * @return a consumer for a session
	 */
	Consumer<S> postEvent(L listener);

	/**
	 * Fabricates a specification listener with the specified pre/post event logic.
	 * @param preEvent a pre-event consumer for a session
	 * @param postEvent a post-event consumer for a session
	 * @return a specification listener implementation
	 */
	L asEventListener(Consumer<S> preEvent, Consumer<S> postEvent);
}
