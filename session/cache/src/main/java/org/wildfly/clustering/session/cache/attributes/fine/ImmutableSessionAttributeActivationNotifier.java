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
import org.wildfly.clustering.session.container.SessionActivationListenerFacadeProvider;

/**
 * Triggers activation/passivation events for a single session attribute.
 * @param <S> the container-specific session facade type
 * @param <C> the session context type
 * @param <L> the container-specific session activation listener type
 * @author Paul Ferraro
 */
public class ImmutableSessionAttributeActivationNotifier<S, C, L> implements SessionAttributeActivationNotifier {

	private final Function<Supplier<L>, L> prePassivateListenerFactory;
	private final Function<Supplier<L>, L> postActivateListenerFactory;
	private final SessionActivationListenerFacadeProvider<S, C, L> provider;
	private final Function<L, Consumer<S>> prePassivateNotifier;
	private final Function<L, Consumer<S>> postActivateNotifier;
	private final S session;
	private final Map<Supplier<L>, L> listeners = new ConcurrentHashMap<>();

	public ImmutableSessionAttributeActivationNotifier(SessionActivationListenerFacadeProvider<S, C, L> provider, ImmutableSession session, C context) {
		this.provider = provider;
		this.prePassivateNotifier = this.provider::prePassivateNotifier;
		this.postActivateNotifier = this.provider::postActivateNotifier;
		this.prePassivateListenerFactory = new SessionActivationListenerFactory<>(provider, true);
		this.postActivateListenerFactory = new SessionActivationListenerFactory<>(provider, false);
		this.session = provider.asSession(session, context);
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
		this.provider.asSessionActivationListener(object).ifPresent(new Consumer<>() {
			@Override
			public void accept(L listener) {
				Supplier<L> reference = new SessionActivationListenerKey<>(listener);
				ImmutableSessionAttributeActivationNotifier.this.listeners.computeIfAbsent(reference, listenerFactory);
				notifierFactory.apply(listener).accept(ImmutableSessionAttributeActivationNotifier.this.session);
			}
		});
	}

	@Override
	public void close() {
		for (L listener : this.listeners.values()) {
			this.provider.prePassivateNotifier(listener).accept(this.session);
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
		private final SessionActivationListenerFacadeProvider<S, C, L> provider;
		private final boolean active;

		SessionActivationListenerFactory(SessionActivationListenerFacadeProvider<S, C, L> provider, boolean active) {
			this.provider = provider;
			this.active = active;
		}

		@Override
		public L apply(Supplier<L> reference) {
			SessionActivationListenerFacadeProvider<S, C, L> provider = this.provider;
			L listener = reference.get();
			// Prevents redundant session activation events for a given listener.
			AtomicBoolean active = new AtomicBoolean(this.active);
			Consumer<S> prePassivate = new Consumer<>() {
				@Override
				public void accept(S session) {
					if (active.compareAndSet(true, false)) {
						provider.prePassivateNotifier(listener).accept(session);
					}
				}
			};
			Consumer<S> postActivate = new Consumer<>() {
				@Override
				public void accept(S session) {
					if (active.compareAndSet(false, true)) {
						provider.postActivateNotifier(listener).accept(session);
					}
				}
			};
			return provider.asSessionActivationListener(prePassivate, postActivate);
		}
	}
}
