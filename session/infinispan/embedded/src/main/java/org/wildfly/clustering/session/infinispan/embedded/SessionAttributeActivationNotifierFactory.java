/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.cache.DetachedSession;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * Factory for creating a SessionAttributeActivationNotifier for a given session identifier.
 * Session activation events will created using detached sessions.
 * @author Paul Ferraro
 * @param <S> the container session type
 * @param <C> the session manager context type
 * @param <L> the activation listener specification type
 * @param <SC> the session context type
 */
public class SessionAttributeActivationNotifierFactory<S, C, L, SC> implements Function<String, SessionAttributeActivationNotifier>, Registrar<Map.Entry<C, SessionManager<SC>>> {

	private final Map<C, SessionManager<SC>> contexts = new ConcurrentHashMap<>();
	private final SessionSpecificationProvider<S, C> sessionProvider;
	private final SessionEventListenerSpecificationProvider<S, L> listenerProvider;
	private final Function<L, Consumer<S>> prePassivateFactory;
	private final Function<L, Consumer<S>> postActivateFactory;

	public SessionAttributeActivationNotifierFactory(SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider) {
		this.sessionProvider = sessionProvider;
		this.listenerProvider = listenerProvider;
		this.prePassivateFactory = listenerProvider::preEvent;
		this.postActivateFactory = listenerProvider::postEvent;
	}

	@Override
	public Registration register(Map.Entry<C, SessionManager<SC>> entry) {
		C context = entry.getKey();
		this.contexts.put(context, entry.getValue());
		return () -> this.contexts.remove(context);
	}

	@Override
	public SessionAttributeActivationNotifier apply(String sessionId) {
		Map<C, SessionManager<SC>> contexts = this.contexts;
		SessionSpecificationProvider<S, C> sessionProvider = this.sessionProvider;
		SessionEventListenerSpecificationProvider<S, L> listenerProvider = this.listenerProvider;
		Function<L, Consumer<S>> prePassivateNotifier = this.prePassivateFactory;
		Function<L, Consumer<S>> postActivateNotifier = this.postActivateFactory;

		return new SessionAttributeActivationNotifier() {
			@Override
			public void prePassivate(Object value) {
				this.notify(prePassivateNotifier, value);
			}

			@Override
			public void postActivate(Object value) {
				this.notify(postActivateNotifier, value);
			}

			public void notify(Function<L, Consumer<S>> notifier, Object value) {
				listenerProvider.asEventListener(value).ifPresent(listener -> {
					for (Map.Entry<C, SessionManager<SC>> entry : contexts.entrySet()) {
						C context = entry.getKey();
						SessionManager<SC> manager = entry.getValue();
						Session<SC> session = new DetachedSession<>(manager, sessionId, null);
						notifier.apply(listener).accept(sessionProvider.asSession(session, context));
					}
				});
			}

			@Override
			public void close() {
				// Nothing to close
			}
		};
	}
}
