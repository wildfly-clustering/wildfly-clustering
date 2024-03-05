/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import java.util.Map;
import java.util.function.Consumer;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;

/**
 * @author Paul Ferraro
 */
public class MockSessionSpecificationProvider<C> implements SessionSpecificationProvider<Map.Entry<ImmutableSession, C>, C>, SessionEventListenerSpecificationProvider<Map.Entry<ImmutableSession, C>, PassivationListener<C>> {

	@SuppressWarnings("unchecked")
	@Override
	public Class<PassivationListener<C>> getEventListenerClass() {
		return (Class<PassivationListener<C>>) (Class<?>) PassivationListener.class;
	}

	@Override
	public Consumer<Map.Entry<ImmutableSession, C>> preEvent(PassivationListener<C> listener) {
		return listener::passivated;
	}

	@Override
	public Consumer<Map.Entry<ImmutableSession, C>> postEvent(PassivationListener<C> listener) {
		return listener::activated;
	}

	@Override
	public PassivationListener<C> asEventListener(Consumer<Map.Entry<ImmutableSession, C>> preEvent, Consumer<Map.Entry<ImmutableSession, C>> postEvent) {
		return new PassivationListener<>() {
			@Override
			public void passivated(Map.Entry<ImmutableSession, C> entry) {
				preEvent.accept(entry);
			}

			@Override
			public void activated(Map.Entry<ImmutableSession, C> entry) {
				postEvent.accept(entry);
			}
		};
	}

	@Override
	public Map.Entry<ImmutableSession, C> asSession(ImmutableSession session, C context) {
		return Map.entry(session, context);
	}
}
