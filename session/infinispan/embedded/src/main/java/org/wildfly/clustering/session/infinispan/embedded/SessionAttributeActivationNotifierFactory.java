/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.Map;
import java.util.function.Consumer;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * Factory for creating a SessionAttributeActivationNotifier for a given session identifier.
 * Session activation events will created using detached sessions.
 * @author Paul Ferraro
 * @param <CC> the container context type
 * @param <S> the container session type
 * @param <L> the container session event listener type
 * @param <SC> the session context type
 */
public class SessionAttributeActivationNotifierFactory<CC, S, L, SC> implements Function<String, SessionAttributeActivationNotifier> {

	private final ContainerProvider<CC, S, L, SC> provider;
	private final Iterable<Map.Entry<CC, SessionManager<SC>>> managers;

	/**
	 * Creates a session attribute notifier of activation events.
	 * @param provider the container provider
	 * @param managers a collection of session managers
	 */
	public SessionAttributeActivationNotifierFactory(ContainerProvider<CC, S, L, SC> provider, Iterable<Map.Entry<CC, SessionManager<SC>>> managers) {
		this.provider = provider;
		this.managers = managers;
	}

	@Override
	public SessionAttributeActivationNotifier apply(String sessionId) {
		ContainerProvider<CC, S, L, SC> provider = this.provider;
		Iterable<Map.Entry<CC, SessionManager<SC>>> managers = this.managers;

		return new SessionAttributeActivationNotifier() {
			@Override
			public void prePassivate(Object value) {
				this.notify(provider::getPrePassivateEventNotifier, value);
			}

			@Override
			public void postActivate(Object value) {
				this.notify(provider::getPostActivateEventNotifier, value);
			}

			public void notify(Function<L, Consumer<S>> notifier, Object value) {
				for (Map.Entry<CC, SessionManager<SC>> entry : managers) {
					CC context = entry.getKey();
					SessionManager<SC> manager = entry.getValue();
					S session = provider.getDetachedSession(manager, sessionId, context);
					if (session != null) {
						provider.getSessionEventListener(session, value).map(notifier).ifPresent(action -> action.accept(session));
					}
				}
			}
		};
	}
}
