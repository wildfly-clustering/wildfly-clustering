/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes;

import java.util.function.Function;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * A notifier of activation/passivation events originating from the session lifecycle.
 * @param <CC> the container context type
 * @param <S> the container session type
 * @param <L> the container session event listener type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class ContainerSessionAttributeActivationNotifier<CC, S, L, SC> implements SessionAttributeActivationNotifier {

	private final ContainerProvider<CC, S, L, SC> provider;
	private final S session;

	/**
	 * Creates a session activation notifier.
	 * @param provider container provider
	 * @param session a detachable session
	 */
	public ContainerSessionAttributeActivationNotifier(ContainerProvider<CC, S, L, SC> provider, S session) {
		this.provider = provider;
		this.session = session;
	}

	@Override
	public void prePassivate(Object object) {
		this.notify(object, this.provider::getPrePassivateEventNotifier);
	}

	@Override
	public void postActivate(Object object) {
		this.notify(object, this.provider::getPostActivateEventNotifier);
	}

	private void notify(Object object, Function<L, Consumer<S>> factory) {
		this.provider.getSessionEventListener(this.session, object).map(factory).orElse(Consumer.empty()).accept(this.session);
	}
}
