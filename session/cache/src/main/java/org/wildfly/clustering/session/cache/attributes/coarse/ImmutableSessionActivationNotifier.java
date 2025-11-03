/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * Triggers activation/passivation events for all attributes of a session.
 * @author Paul Ferraro
 * @param <CC> the container context type
 * @param <S> the container session type
 * @param <L> the container listener type
 * @param <SC> the session context type
 */
public class ImmutableSessionActivationNotifier<CC, S, L, SC> implements SessionActivationNotifier {

	private final ContainerProvider<CC, S, L, SC> provider;
	private final S session;
	private final Collection<Object> attributes;
	private final AtomicBoolean active = new AtomicBoolean(false);

	/**
	 * Create a activation notifier for an immutable session.
	 * @param provider the container provider
	 * @param session an immutable session
	 * @param attributes the attributes of this session
	 */
	public ImmutableSessionActivationNotifier(ContainerProvider<CC, S, L, SC> provider, S session, Collection<Object> attributes) {
		this.provider = provider;
		this.session = session;
		this.attributes = attributes;
	}

	@Override
	public void prePassivate() {
		if (this.active.compareAndSet(true, false)) {
			this.notify(this.provider::getPrePassivateEventNotifier);
		}
	}

	@Override
	public void postActivate() {
		if (this.active.compareAndSet(false, true)) {
			this.notify(this.provider::getPostActivateEventNotifier);
		}
	}

	private void notify(Function<L, Consumer<S>> factory) {
		List<L> listeners = this.attributes.stream().map(attribute -> this.provider.getSessionEventListener(this.session, attribute)).filter(Optional::isPresent).map(Optional::get).toList();
		if (!listeners.isEmpty()) {
			for (L listener : listeners) {
				factory.apply(listener).accept(this.session);
			}
		}
	}
}
