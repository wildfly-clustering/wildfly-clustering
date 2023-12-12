/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionAttributes;
import org.wildfly.clustering.session.container.SessionActivationListenerFacadeProvider;

/**
 * Triggers activation/passivation events for all attributes of a session.
 * @param <S> the HttpSession specification type
 * @param <C> the ServletContext specification type
 * @param <L> the HttpSessionActivationListener specification type
 * @author Paul Ferraro
 */
public class ImmutableSessionActivationNotifier<S, C, L> implements SessionActivationNotifier {

	private final SessionActivationListenerFacadeProvider<S, C, L> provider;
	private final ImmutableSession session;
	private final C context;
	private final AtomicBoolean active = new AtomicBoolean(false);
	private final Function<L, Consumer<S>> prePassivateNotifier;
	private final Function<L, Consumer<S>> postActivateNotifier;

	public ImmutableSessionActivationNotifier(SessionActivationListenerFacadeProvider<S, C, L> provider, ImmutableSession session, C context) {
		this.provider = provider;
		this.session = session;
		this.context = context;
		this.prePassivateNotifier = this.provider::prePassivateNotifier;
		this.postActivateNotifier = this.provider::postActivateNotifier;
	}

	@Override
	public void prePassivate() {
		if (this.active.compareAndSet(true, false)) {
			this.notify(this.prePassivateNotifier);
		}
	}

	@Override
	public void postActivate() {
		if (this.active.compareAndSet(false, true)) {
			this.notify(this.postActivateNotifier);
		}
	}

	private void notify(Function<L, Consumer<S>> notifierFactory) {
		ImmutableSessionAttributes attributes = this.session.getAttributes();
		Set<String> attributeNames = attributes.getAttributeNames();
		if (!attributeNames.isEmpty()) {
			List<L> listeners = new ArrayList<>(attributeNames.size());
			for (String attributeName : attributeNames) {
				Object attributeValue = attributes.getAttribute(attributeName);
				if (attributeValue != null) {
					this.provider.asSessionActivationListener(attributeValue).ifPresent(listeners::add);
				}
			}
			if (!listeners.isEmpty()) {
				S session = this.provider.asSession(this.session, this.context);
				for (L listener : listeners) {
					notifierFactory.apply(listener).accept(session);
				}
			}
		}
	}
}
