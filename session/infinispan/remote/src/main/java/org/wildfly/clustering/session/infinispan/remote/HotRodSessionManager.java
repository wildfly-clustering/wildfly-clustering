/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.Set;
import java.util.function.Consumer;

import org.wildfly.clustering.cache.batch.Batcher;
import org.wildfly.clustering.cache.infinispan.batch.TransactionBatch;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.cache.AbstractSessionManager;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.common.function.Functions;

/**
 * Generic HotRod-based session manager implementation - independent of cache mapping strategy.
 * @param <C> the session manager context type
 * @param <MV> the meta-data value type
 * @param <AV> the attributes value type
 * @param <SC> the session context type
 * @author Paul Ferraro
 */
public class HotRodSessionManager<C, MV, AV, SC> extends AbstractSessionManager<C, MV, AV, SC, TransactionBatch> {
	private final Registrar<Consumer<ImmutableSession>> expirationListenerRegistrar;
	private final Consumer<ImmutableSession> expirationListener;
	private final Batcher<TransactionBatch> batcher;

	private volatile Registration expirationListenerRegistration;

	public HotRodSessionManager(SessionManagerConfiguration<C> configuration, SessionFactory<C, MV, AV, SC> factory, HotRodSessionManagerConfiguration hotrod) {
		super(configuration, hotrod, factory, Functions.discardingConsumer());
		this.expirationListenerRegistrar = hotrod.getExpirationListenerRegistrar();
		this.expirationListener = configuration.getExpirationListener();
		this.batcher = hotrod.getBatcher();
	}

	@Override
	public void start() {
		this.expirationListenerRegistration = this.expirationListenerRegistrar.register(this.expirationListener);
	}

	@Override
	public void stop() {
		if (this.expirationListenerRegistration != null) {
			this.expirationListenerRegistration.close();
		}
	}

	@Override
	public Batcher<TransactionBatch> getBatcher() {
		return this.batcher;
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
