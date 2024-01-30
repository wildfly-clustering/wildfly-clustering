/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.util.Map;
import java.util.function.Consumer;

import org.wildfly.clustering.session.container.ContainerFacadeProvider;

/**
 * @author Paul Ferraro
 */
public class MockContainerFacadeProvider<DC> implements ContainerFacadeProvider<Map.Entry<ImmutableSession, DC>, DC, PassivationListener<DC>> {

	@Override
	public Consumer<Map.Entry<ImmutableSession, DC>> prePassivateNotifier(PassivationListener<DC> listener) {
		return listener::passivated;
	}

	@Override
	public Consumer<Map.Entry<ImmutableSession, DC>> postActivateNotifier(PassivationListener<DC> listener) {
		return listener::activated;
	}

	@Override
	public PassivationListener<DC> asSessionActivationListener(Consumer<Map.Entry<ImmutableSession, DC>> prePassivate, Consumer<Map.Entry<ImmutableSession, DC>> postActivate) {
		return new PassivationListener<>() {
			@Override
			public void passivated(Map.Entry<ImmutableSession, DC> entry) {
				prePassivate.accept(entry);
			}

			@Override
			public void activated(Map.Entry<ImmutableSession, DC> entry) {
				postActivate.accept(entry);
			}
		};
	}

	@Override
	public Map.Entry<ImmutableSession, DC> asSession(ImmutableSession session, DC context) {
		return Map.entry(session, context);
	}
}
