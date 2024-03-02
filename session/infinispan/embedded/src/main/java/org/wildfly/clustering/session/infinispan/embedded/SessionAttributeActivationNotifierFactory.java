/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.cache.DetachedSession;
import org.wildfly.clustering.session.cache.attributes.fine.SessionAttributeActivationNotifier;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * Factory for creating a SessionAttributeActivationNotifier for a given session identifier.
 * Session activation events will created using OOB sessions.
 * @author Paul Ferraro
 * @param <S> the container session type
 * @param <DC> the deployment context type
 * @param <L> the activation listener specification type
 * @param <SC> the session context type
 * @param <B> the batch type
 */
public class SessionAttributeActivationNotifierFactory<S, DC, L, SC, B extends Batch> implements Function<String, SessionAttributeActivationNotifier>, Registrar<Map.Entry<DC, SessionManager<SC, B>>> {

	private final Map<DC, SessionManager<SC, B>> contexts = new ConcurrentHashMap<>();
	private final SessionSpecificationProvider<S, DC, L> provider;
	private final Function<L, Consumer<S>> prePassivateFactory;
	private final Function<L, Consumer<S>> postActivateFactory;

	public SessionAttributeActivationNotifierFactory(SessionSpecificationProvider<S, DC, L> provider) {
		this.provider = provider;
		this.prePassivateFactory = provider::prePassivate;
		this.postActivateFactory = provider::postActivate;
	}

	@Override
	public Registration register(Map.Entry<DC, SessionManager<SC, B>> entry) {
		DC context = entry.getKey();
		this.contexts.put(context, entry.getValue());
		return () -> this.contexts.remove(context);
	}

	@Override
	public SessionAttributeActivationNotifier apply(String sessionId) {
		Map<DC, SessionManager<SC, B>> contexts = this.contexts;
		SessionSpecificationProvider<S, DC, L> provider = this.provider;
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
				Optional<L> listener = provider.asSessionActivationListener(value);
				if (listener.isPresent()) {
					for (Map.Entry<DC, SessionManager<SC, B>> entry : contexts.entrySet()) {
						DC context = entry.getKey();
						SessionManager<SC, B> manager = entry.getValue();
						Session<SC> session = new DetachedSession<>(manager, sessionId, null);
						notifier.apply(listener.get()).accept(provider.asSession(session, context));
					}
				}
			}

			@Override
			public void close() {
				// Nothing to close
			}
		};
	}
}
