/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.Optional;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;

/**
 * Provider of container specific facades.
 * @author Paul Ferraro
 * @param <CC> the container context type
 * @param <S> the container session type
 * @param <L> the container session event listener type
 * @param <SC> the session context type
 */
public interface ContainerProvider<CC, S, L, SC> {
	/**
	 * A provider that does not emit passivation/activation events.
	 * @param <CC> the container context type
	 * @param <S> the container session type
	 * @param <SC> the session context type
	 */
	interface VoidEventListener<CC, S, SC> extends ContainerProvider<CC, S, Void, SC> {
		@Override
		default Optional<Void> getSessionEventListener(S session, Object attribute) {
			return Optional.empty();
		}

		@Override
		default Consumer<S> getPostActivateEventNotifier(Void listener) {
			return Consumer.of();
		}

		@Override
		default Consumer<S> getPrePassivateEventNotifier(Void listener) {
			return Consumer.of();
		}
	}

	/**
	 * A provider where session attributes implement the passivation/activation event listener.
	 * @param <CC> the container context type
	 * @param <S> the container session type
	 * @param <L> the container session event listener type
	 * @param <SC> the session context type
	 */
	interface SessionAttributeEventListener<CC, S, L, SC> extends ContainerProvider<CC, S, L, SC> {
		@Override
		default Optional<L> getSessionEventListener(S session, Object attribute) {
			Class<L> listenerClass = this.getSessionEventListenerClass();
			return Optional.ofNullable(attribute).filter(listenerClass::isInstance).map(listenerClass::cast);
		}

		/**
		 * Returns the specification type of the session event listener implemented by interested session attributes.
		 * @return the specification type of the session event listener implemented by interested session attributes.
		 */
		Class<L> getSessionEventListenerClass();
	}

	/**
	 * Returns a unique identifier of the specified context.
	 * @param context a container context
	 * @return a unique identifier of the specified context.
	 */
	String getId(CC context);

	/**
	 * Returns a container facade to an immutable session.
	 * @param manager a session manager
	 * @param session an immutable session
	 * @param context a container context
	 * @return a container facade to a detached session.
	 */
	S getSession(SessionManager<SC> manager, ImmutableSession session, CC context);

	/**
	 * Returns a container facade to a session.
	 * @param manager a session manager
	 * @param session a session
	 * @param context a container context
	 * @return a container facade to a detached session.
	 */
	S getSession(SessionManager<SC> manager, Session<SC> session, CC context);

	/**
	 * Returns a container facade to a session reference.
	 * @param reference a session reference
	 * @param id a session identifier
	 * @param context a container context
	 * @return a container facade to a detached session.
	 */
	S getSession(Reference<Session<SC>> reference, String id, CC context);

	/**
	 * Returns the container specific activation/passivation listener for the specified attribute of the specified session, if one exists.
	 * @param session the session event source
	 * @param attribute the session attribute event source
	 * @return the container specific activation/passivation listener for the specified attribute of the specified session, if one exists.
	 */
	Optional<L> getSessionEventListener(S session, Object attribute);

	/**
	 * Returns the pre-passivation event notifier for the specified session event listener.
	 * @param listener the specification listener
	 * @return the consumer for a session
	 */
	Consumer<S> getPrePassivateEventNotifier(L listener);

	/**
	 * Returns a post-activation event notifier for the specified session event listener.
	 * @param listener the specification listener
	 * @return a consumer for a session
	 */
	Consumer<S> getPostActivateEventNotifier(L listener);
}
