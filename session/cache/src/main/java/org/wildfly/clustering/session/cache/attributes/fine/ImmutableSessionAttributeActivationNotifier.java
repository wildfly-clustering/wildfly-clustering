/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.fine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * Triggers activation/passivation events for a single session attribute.
 * @param <CC> the container context type
 * @param <S> the container session type
 * @param <L> the container session event listener type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class ImmutableSessionAttributeActivationNotifier<CC, S, L, SC> implements SessionAttributeActivationNotifier {

	private final Function<Supplier<L>, Optional<L>> prePassivateListenerFactory;
	private final Function<Supplier<L>, Optional<L>> postActivateListenerFactory;
	private final ContainerProvider<CC, S, L, SC> provider;
	private final S session;
	private final Map<Supplier<L>, Optional<L>> listeners = new ConcurrentHashMap<>();

	/**
	 * Creates a session activation notifier.
	 * @param provider container provider
	 * @param session a detachable session
	 */
	public ImmutableSessionAttributeActivationNotifier(ContainerProvider<CC, S, L, SC> provider, S session) {
		this.provider = provider;
		this.session = session;
		this.prePassivateListenerFactory = new SessionActivationListenerFactory<>(provider, true);
		this.postActivateListenerFactory = new SessionActivationListenerFactory<>(provider, false);
	}

	@Override
	public void prePassivate(Object object) {
		this.notify(object, this.prePassivateListenerFactory, this.provider::getPrePassivateEventNotifier);
	}

	@Override
	public void postActivate(Object object) {
		this.notify(object, this.postActivateListenerFactory, this.provider::getPostActivateEventNotifier);
	}

	private void notify(Object object, Function<Supplier<L>, Optional<L>> listenerFactory, Function<L, Consumer<S>> notifierFactory) {
		this.provider.getSessionEventListener(this.session, object).ifPresent(listener -> {
			Supplier<L> reference = new SessionActivationListenerKey<>(listener);
			this.listeners.computeIfAbsent(reference, listenerFactory);
			notifierFactory.apply(listener).accept(this.session);
		});
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
			if (!(object instanceof SessionActivationListenerKey reference)) return false;
			return this.listener == reference.listener;
		}
	}

	/**
	 * Factory for creating HttpSessionActivationListener decorators.
	 */
	private static class SessionActivationListenerFactory<CC, S, L, SC> implements Function<Supplier<L>, Optional<L>> {
		private final ContainerProvider<CC, S, L, SC> provider;
		private final AtomicBoolean active;

		SessionActivationListenerFactory(ContainerProvider<CC, S, L, SC> provider, boolean active) {
			this.provider = provider;
			this.active = new AtomicBoolean(active);
		}

		@Override
		public Optional<L> apply(Supplier<L> listener) {
			// Prevents redundant session activation events for a given listener.
			Consumer<S> prePassivate = session -> {
				if (this.active.compareAndSet(true, false)) {
					this.provider.getPrePassivateEventNotifier(listener.get()).accept(session);
				}
			};
			Consumer<S> postActivate = session -> {
				if (this.active.compareAndSet(false, true)) {
					this.provider.getPostActivateEventNotifier(listener.get()).accept(session);
				}
			};
			return this.provider.getSessionEventListener(prePassivate, postActivate);
		}
	}
}
