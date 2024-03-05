/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * Triggers activation/passivation events for all attributes of a session.
 * @param <S> the HttpSession specification type
 * @param <C> the ServletContext specification type
 * @param <L> the HttpSessionActivationListener specification type
 * @author Paul Ferraro
 */
public class ImmutableSessionActivationNotifier<S, C, L> implements SessionActivationNotifier {

	private final SessionSpecificationProvider<S, C> sessionProvider;
	private final SessionEventListenerSpecificationProvider<S, L> listenerProvider;
	private final ImmutableSession session;
	private final C context;
	private final AtomicBoolean active = new AtomicBoolean(false);
	private final Function<L, Consumer<S>> prePassivateFactory;
	private final Function<L, Consumer<S>> postActivateFactory;

	public ImmutableSessionActivationNotifier(SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider, ImmutableSession session, C context) {
		this.sessionProvider = sessionProvider;
		this.listenerProvider = listenerProvider;
		this.session = session;
		this.context = context;
		this.prePassivateFactory = listenerProvider::preEvent;
		this.postActivateFactory = listenerProvider::postEvent;
	}

	@Override
	public void prePassivate() {
		if (this.active.compareAndSet(true, false)) {
			this.notify(this.prePassivateFactory);
		}
	}

	@Override
	public void postActivate() {
		if (this.active.compareAndSet(false, true)) {
			this.notify(this.postActivateFactory);
		}
	}

	private void notify(Function<L, Consumer<S>> factory) {
		List<L> listeners = this.session.getAttributes().values().stream().map(this.listenerProvider::asEventListener).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		if (!listeners.isEmpty()) {
			S session = this.sessionProvider.asSession(this.session, this.context);
			for (L listener : listeners) {
				factory.apply(listener).accept(session);
			}
		}
	}
}
