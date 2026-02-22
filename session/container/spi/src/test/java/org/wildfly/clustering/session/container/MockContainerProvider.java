/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionMetaData;

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
	public Session<SC> getSession(SessionManager<SC> manager, ImmutableSession session, String context) {
		return new Session<>() {
			@Override
			public String getId() {
				return session.getId();
			}

			@Override
			public boolean isValid() {
				return session.isValid();
			}

			@Override
			public Map<String, Object> getAttributes() {
				return session.getAttributes();
			}

			@Override
			public SessionMetaData getMetaData() {
				ImmutableSessionMetaData metaData = session.getMetaData();
				return new SessionMetaData() {
					@Override
					public Instant getCreationTime() {
						return metaData.getCreationTime();
					}

					@Override
					public Optional<Instant> getLastAccessStartTime() {
						return metaData.getLastAccessStartTime();
					}

					@Override
					public Optional<Instant> getLastAccessEndTime() {
						return metaData.getLastAccessEndTime();
					}

					@Override
					public Optional<Duration> getMaxIdle() {
						return metaData.getMaxIdle();
					}

					@Override
					public void setLastAccess(Instant startTime, Instant endTime) {
					}

					@Override
					public void setMaxIdle(Duration maxIdle) {
					}
				};
			}

			@Override
			public void invalidate() {
			}

			@Override
			public SC getContext() {
				return null;
			}

			@Override
			public void close() {
			}
		};
	}

	@Override
	public Session<SC> getSession(SessionManager<SC> manager, Session<SC> session, String context) {
		return session;
	}

	@Override
	public Session<SC> getSession(Reference<Session<SC>> reference, String id, String context) {
		return new Session<>() {
			@Override
			public String getId() {
				return id;
			}

			@Override
			public boolean isValid() {
				return reference.getReader().read(Function.identity()).isValid();
			}

			@Override
			public Map<String, Object> getAttributes() {
				return reference.getReader().read(Function.identity()).getAttributes();
			}

			@Override
			public SessionMetaData getMetaData() {
				return reference.getReader().read(Function.identity()).getMetaData();
			}

			@Override
			public void invalidate() {
				reference.getReader().read(Function.identity()).invalidate();
			}

			@Override
			public SC getContext() {
				return reference.getReader().read(Function.identity()).getContext();
			}

			@Override
			public void close() {
			}
		};
	}

	@Override
	public Consumer<Session<SC>> getPrePassivateEventNotifier(PassivationListener<SC> listener) {
		return listener::passivated;
	}

	@Override
	public Consumer<Session<SC>> getPostActivateEventNotifier(PassivationListener<SC> listener) {
		return listener::activated;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<PassivationListener<SC>> getSessionEventListenerClass() {
		return (Class<PassivationListener<SC>>) (Class<?>) PassivationListener.class;
	}
}
