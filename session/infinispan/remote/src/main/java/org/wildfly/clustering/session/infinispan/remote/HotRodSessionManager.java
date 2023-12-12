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
import org.wildfly.clustering.session.cache.AbstractSessionManager;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.common.function.Functions;

/**
 * Generic HotRod-based session manager implementation - independent of cache mapping strategy.
 * @param <SC> the deployment context type
 * @param <MV> the meta-data value type
 * @param <AV> the attributes value type
 * @param <LC> the local context type
 * @author Paul Ferraro
 */
public class HotRodSessionManager<SC, MV, AV, LC> extends AbstractSessionManager<SC, MV, AV, LC, TransactionBatch> {
	private final Registrar<Consumer<ImmutableSession>> expirationListenerRegistrar;
	private final Consumer<ImmutableSession> expirationListener;
	private final Batcher<TransactionBatch> batcher;

	private volatile Registration expirationListenerRegistration;

	public HotRodSessionManager(HotRodSessionManagerConfiguration<SC> configuration, SessionFactory<SC, MV, AV, LC> factory) {
		super(configuration, factory, Functions.discardingConsumer());
		this.expirationListenerRegistrar = configuration.getExpirationListenerRegistrar();
		this.expirationListener = configuration.getExpirationListener();
		this.batcher = configuration.getBatcher();
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
