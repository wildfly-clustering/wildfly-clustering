/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.attributes;

import java.util.Map;

import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.cache.attributes.ContainerSessionAttributeActivationNotifier;
import org.wildfly.clustering.session.cache.attributes.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * A notifier of session activation/passivation events originating from Infinispan.
 * @author Paul Ferraro
 * @param <CC> the container context type
 * @param <S> the container session type
 * @param <L> the container session event listener type
 * @param <SC> the session context type
 */
public class CompositeContainerSessionAttributeActivationNotifier<CC, S, L, SC> implements SessionAttributeActivationNotifier {

	private final ContainerProvider<CC, S, L, SC> provider;
	private final Iterable<Map.Entry<CC, SessionManager<SC>>> managers;
	private final String sessionId;

	/**
	 * Creates a session attribute activation notifier.
	 * @param provider the container provider
	 * @param managers the container context and their managers
	 * @param sessionId the identifier of the session to be notified.
	 */
	public CompositeContainerSessionAttributeActivationNotifier(ContainerProvider<CC, S, L, SC> provider, Iterable<Map.Entry<CC, SessionManager<SC>>> managers, String sessionId) {
		this.provider = provider;
		this.managers = managers;
		this.sessionId = sessionId;
	}

	@Override
	public void prePassivate(Object value) {
		this.notify(SessionAttributeActivationNotifier::prePassivate, value);
	}

	@Override
	public void postActivate(Object value) {
		this.notify(SessionAttributeActivationNotifier::postActivate, value);
	}

	private void notify(BiConsumer<SessionAttributeActivationNotifier, Object> notification, Object value) {
		// We don't know the container context associated with the session, so try them all
		// If the session is shared across contexts, then an event will be triggered for each
		for (Map.Entry<CC, SessionManager<SC>> entry : this.managers) {
			CC context = entry.getKey();
			SessionManager<SC> manager = entry.getValue();
			S session = this.provider.getDetachedSession(manager, this.sessionId, context);
			if (session != null) {
				notification.accept(new ContainerSessionAttributeActivationNotifier<>(this.provider, session), value);
			}
		}
	}
}
