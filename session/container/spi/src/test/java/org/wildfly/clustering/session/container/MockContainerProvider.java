/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.Optional;
import java.util.function.Consumer;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;

/**
 * A mock container provider for unit testing.
 * @author Paul Ferraro
 * @param <SC> the session context type
 */
@MetaInfServices(ContainerProvider.class)
public class MockContainerProvider<SC> implements ContainerProvider.SessionAttributeEventListener<String, Session<SC>, PassivationListener<SC>, SC> {

	@Override
	public String getId(String context) {
		return context;
	}

	@Override
	public Session<SC> getDetachedSession(SessionManager<SC> manager, String id, String context) {
		return manager.getDetachedSession(id);
	}

	@Override
	public Consumer<Session<SC>> getPrePassivateEventNotifier(PassivationListener<SC> listener) {
		return listener::passivated;
	}

	@Override
	public Consumer<Session<SC>> getPostActivateEventNotifier(PassivationListener<SC> listener) {
		return listener::activated;
	}

	@Override
	public Optional<PassivationListener<SC>> getSessionEventListener(Consumer<Session<SC>> prePassivateEventNotifier, Consumer<Session<SC>> postActivateEventNotifier) {
		return Optional.of(new PassivationListener<>() {
			@Override
			public void passivated(Session<SC> session) {
				prePassivateEventNotifier.accept(session);
			}

			@Override
			public void activated(Session<SC> session) {
				postActivateEventNotifier.accept(session);
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<PassivationListener<SC>> getSessionEventListenerClass() {
		return (Class<PassivationListener<SC>>) (Class<?>) PassivationListener.class;
	}
}
