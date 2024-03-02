/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.function.Consumer;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * @author Paul Ferraro
 */
public class MockSessionSpecificationProvider<C> implements SessionSpecificationProvider<Map.Entry<ImmutableSession, C>, C, PassivationListener<C>> {

	@Override
	public Consumer<Map.Entry<ImmutableSession, C>> prePassivate(PassivationListener<C> listener) {
		return listener::passivated;
	}

	@Override
	public Consumer<Map.Entry<ImmutableSession, C>> postActivate(PassivationListener<C> listener) {
		return listener::activated;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<PassivationListener<C>> getSessionActivationListenerClass() {
		return (Class<PassivationListener<C>>) (Class<?>) PassivationListener.class;
	}

	@Override
	public PassivationListener<C> asSessionActivationListener(Consumer<Map.Entry<ImmutableSession, C>> prePassivate, Consumer<Map.Entry<ImmutableSession, C>> postActivate) {
		return new PassivationListener<>() {
			@Override
			public void passivated(Map.Entry<ImmutableSession, C> entry) {
				prePassivate.accept(entry);
			}

			@Override
			public void activated(Map.Entry<ImmutableSession, C> entry) {
				postActivate.accept(entry);
			}
		};
	}

	@Override
	public Map.Entry<ImmutableSession, C> asSession(ImmutableSession session, C context) {
		return Map.entry(session, context);
	}
}
