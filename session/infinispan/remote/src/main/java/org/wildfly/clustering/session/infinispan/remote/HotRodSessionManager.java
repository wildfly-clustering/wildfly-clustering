/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.cache.AbstractSessionManager;
import org.wildfly.clustering.session.cache.SessionFactory;

/**
 * Generic HotRod-based session manager implementation - independent of cache mapping strategy.
 * @param <C> the session manager context type
 * @param <MV> the meta-data value type
 * @param <AV> the attributes value type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManager<C, MV, AV, SC> extends AbstractSessionManager<C, MV, AV, SC> {
	private final Registrar<java.util.function.Consumer<ImmutableSession>> expirationListenerRegistrar;
	private final java.util.function.Consumer<ImmutableSession> expirationListener;
	private final Supplier<Batch> batchFactory;

	private AtomicReference<Registration> expirationListenerRegistration = new AtomicReference<>();

	public HotRodSessionManager(Supplier<SessionManager<SC>> manager, SessionManagerConfiguration<C> configuration, SessionFactory<C, MV, AV, SC> factory, HotRodSessionManagerConfiguration hotrod) {
		super(manager, configuration, hotrod, factory, Consumer.empty());
		this.expirationListenerRegistrar = hotrod.getExpirationListenerRegistrar();
		this.expirationListener = configuration.getExpirationListener();
		this.batchFactory = hotrod.getBatchFactory();
	}

	@Override
	public boolean isStarted() {
		return this.expirationListenerRegistration.get() != null;
	}

	@Override
	public void start() {
		this.expirationListenerRegistration.set(this.expirationListenerRegistrar.register(this.expirationListener));
	}

	@Override
	public void stop() {
		Optional.ofNullable(this.expirationListenerRegistration.getAndSet(null)).ifPresent(Registration::close);
	}

	@Override
	public Supplier<Batch> getBatchFactory() {
		return this.batchFactory;
	}

	@Override
	public Set<String> getActiveSessions() {
		return Set.of();
	}

	@Override
	public Set<String> getSessions() {
		return Set.of();
	}
}
