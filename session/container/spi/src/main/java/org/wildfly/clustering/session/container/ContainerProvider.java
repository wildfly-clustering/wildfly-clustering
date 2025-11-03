/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.Optional;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.session.ImmutableSession;
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
			return Consumer.empty();
		}

		@Override
		default Consumer<S> getPrePassivateEventNotifier(Void listener) {
			return Consumer.empty();
		}

		@Override
		default Optional<Void> getSessionEventListener(Consumer<S> prePassivateEventNotifier, Consumer<S> postActivateEventNotifier) {
			return Optional.empty();
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
	 * Returns a container facade to a detachable session.
	 * @param manager a session manager
	 * @param session a session identifier
	 * @param context a container context
	 * @return a container facade to a detached session.
	 */
	default S getDetachableSession(SessionManager<SC> manager, ImmutableSession session, CC context) {
		return this.getDetachedSession(manager, session.getId(), context);
	}

	/**
	 * Returns a container facade to a detached session.
	 * @param manager a session manager
	 * @param id a session identifier
	 * @param context a container context
	 * @return a container facade to a detached session.
	 */
	S getDetachedSession(SessionManager<SC> manager, String id, CC context);

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

	/**
	 * Composes a specification listener with the specified pre/post event logic.
	 * @param prePassivateEventNotifier a pre-event consumer for a session
	 * @param postActivateEventNotifier a post-event consumer for a session
	 * @return a specification listener implementation
	 */
	Optional<L> getSessionEventListener(Consumer<S> prePassivateEventNotifier, Consumer<S> postActivateEventNotifier);
}
