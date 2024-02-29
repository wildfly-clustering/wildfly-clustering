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
import org.wildfly.clustering.session.container.SessionActivationListenerFacadeProvider;

/**
 * Factory for creating a SessionAttributeActivationNotifier for a given session identifier.
 * Session activation events will created using OOB sessions.
 * @author Paul Ferraro
 * @param <S> the HttpSession specification type
 * @param <SC> the ServletContext specification type
 * @param <AL> the HttpSessionActivationListener specification type
 * @param <LC> the local context type
 * @param <B> the batch type
 */
public class SessionAttributeActivationNotifierFactory<S, SC, AL, LC, B extends Batch> implements Function<String, SessionAttributeActivationNotifier>, Registrar<Map.Entry<SC, SessionManager<LC, B>>> {

	private final Map<SC, SessionManager<LC, B>> contexts = new ConcurrentHashMap<>();
	private final SessionActivationListenerFacadeProvider<S, SC, AL> provider;
	private final Function<AL, Consumer<S>> prePassivateNotifier;
	private final Function<AL, Consumer<S>> postActivateNotifier;

	public SessionAttributeActivationNotifierFactory(SessionActivationListenerFacadeProvider<S, SC, AL> provider) {
		this.provider = provider;
		this.prePassivateNotifier = provider::prePassivateNotifier;
		this.postActivateNotifier = provider::postActivateNotifier;
	}

	@Override
	public Registration register(Map.Entry<SC, SessionManager<LC, B>> entry) {
		SC context = entry.getKey();
		this.contexts.put(context, entry.getValue());
		return () -> this.contexts.remove(context);
	}

	@Override
	public SessionAttributeActivationNotifier apply(String sessionId) {
		Map<SC, SessionManager<LC, B>> contexts = this.contexts;
		SessionActivationListenerFacadeProvider<S, SC, AL> provider = this.provider;
		Function<AL, Consumer<S>> prePassivateNotifier = this.prePassivateNotifier;
		Function<AL, Consumer<S>> postActivateNotifier = this.postActivateNotifier;

		return new SessionAttributeActivationNotifier() {
			@Override
			public void prePassivate(Object value) {
				this.notify(prePassivateNotifier, value);
			}

			@Override
			public void postActivate(Object value) {
				this.notify(postActivateNotifier, value);
			}

			public void notify(Function<AL, Consumer<S>> notifier, Object value) {
				Optional<AL> listener = provider.asSessionActivationListener(value);
				if (listener.isPresent()) {
					for (Map.Entry<SC, SessionManager<LC, B>> entry : contexts.entrySet()) {
						SC context = entry.getKey();
						SessionManager<LC, B> manager = entry.getValue();
						Session<LC> session = new DetachedSession<>(manager, sessionId, null);
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
