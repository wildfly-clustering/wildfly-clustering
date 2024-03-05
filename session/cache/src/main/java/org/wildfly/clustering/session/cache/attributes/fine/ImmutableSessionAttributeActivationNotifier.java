/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * Triggers activation/passivation events for a single session attribute.
 * @param <S> the session specification type
 * @param <C> the session manager context specification type
 * @param <L> the session activation listener specification type
 * @author Paul Ferraro
 */
public class ImmutableSessionAttributeActivationNotifier<S, C, L> implements SessionAttributeActivationNotifier {

	private final Function<Supplier<L>, L> prePassivateListenerFactory;
	private final Function<Supplier<L>, L> postActivateListenerFactory;
	private final SessionEventListenerSpecificationProvider<S, L> provider;
	private final Function<L, Consumer<S>> prePassivateNotifier;
	private final Function<L, Consumer<S>> postActivateNotifier;
	private final S session;
	private final Map<Supplier<L>, L> listeners = new ConcurrentHashMap<>();

	public ImmutableSessionAttributeActivationNotifier(SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider, ImmutableSession session, C context) {
		this.provider = listenerProvider;
		this.prePassivateNotifier = listenerProvider::preEvent;
		this.postActivateNotifier = listenerProvider::postEvent;
		this.prePassivateListenerFactory = new SessionActivationListenerFactory<>(listenerProvider, true);
		this.postActivateListenerFactory = new SessionActivationListenerFactory<>(listenerProvider, false);
		this.session = sessionProvider.asSession(session, context);
	}

	@Override
	public void prePassivate(Object object) {
		this.notify(object, this.prePassivateListenerFactory, this.prePassivateNotifier);
	}

	@Override
	public void postActivate(Object object) {
		this.notify(object, this.postActivateListenerFactory, this.postActivateNotifier);
	}

	private void notify(Object object, Function<Supplier<L>, L> listenerFactory, Function<L, Consumer<S>> notifierFactory) {
		this.provider.asEventListener(object).ifPresent(listener -> {
			Supplier<L> reference = new SessionActivationListenerKey<>(listener);
			this.listeners.computeIfAbsent(reference, listenerFactory);
			notifierFactory.apply(listener).accept(ImmutableSessionAttributeActivationNotifier.this.session);
		});
	}

	@Override
	public void close() {
		for (L listener : this.listeners.values()) {
			this.provider.preEvent(listener).accept(this.session);
		}
		this.listeners.clear();
	}

	/**
	 * Map key of a {@link HttpSessionActivationListener} that uses identity equality.
	 */
	private static class SessionActivationListenerKey<L> implements Supplier<L> {
		private final L listener;

		SessionActivationListenerKey(L listener) {
			this.listener = listener;
		}

		@Override
		public L get() {
			return this.listener;
		}

		@Override
		public int hashCode() {
			return this.listener.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof SessionActivationListenerKey)) return false;
			@SuppressWarnings("unchecked")
			SessionActivationListenerKey<L> reference = (SessionActivationListenerKey<L>) object;
			return this.listener == reference.listener;
		}
	}

	/**
	 * Factory for creating HttpSessionActivationListener values.
	 */
	private static class SessionActivationListenerFactory<S, C, L> implements Function<Supplier<L>, L> {
		private final SessionEventListenerSpecificationProvider<S, L> provider;
		private final boolean active;

		SessionActivationListenerFactory(SessionEventListenerSpecificationProvider<S, L> provider, boolean active) {
			this.provider = provider;
			this.active = active;
		}

		@Override
		public L apply(Supplier<L> reference) {
			SessionEventListenerSpecificationProvider<S, L> provider = this.provider;
			L listener = reference.get();
			// Prevents redundant session activation events for a given listener.
			AtomicBoolean active = new AtomicBoolean(this.active);
			Consumer<S> prePassivate = new Consumer<>() {
				@Override
				public void accept(S session) {
					if (active.compareAndSet(true, false)) {
						provider.preEvent(listener).accept(session);
					}
				}
			};
			Consumer<S> postActivate = new Consumer<>() {
				@Override
				public void accept(S session) {
					if (active.compareAndSet(false, true)) {
						provider.postEvent(listener).accept(session);
					}
				}
			};
			return provider.asEventListener(prePassivate, postActivate);
		}
	}
}
