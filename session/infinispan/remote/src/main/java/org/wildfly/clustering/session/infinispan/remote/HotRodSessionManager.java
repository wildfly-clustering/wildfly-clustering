/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.Registrar;
import org.wildfly.clustering.server.Registration;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.cache.AbstractSessionManager;
import org.wildfly.clustering.session.cache.SessionFactory;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionCreationMetaDataKey;

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
	private final RemoteCache<Key<String>, ?> cache;

	private AtomicReference<Registration> expirationListenerRegistration = new AtomicReference<>();

	public HotRodSessionManager(Supplier<SessionManager<SC>> manager, SessionManagerConfiguration<C> configuration, SessionFactory<C, MV, AV, SC> factory, HotRodSessionManagerConfiguration hotrod) {
		super(manager, configuration, hotrod, factory, Consumer.empty());
		this.expirationListenerRegistrar = hotrod.getExpirationListenerRegistrar();
		this.expirationListener = configuration.getExpirationListener();
		this.cache = hotrod.getCache();
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
	public Set<String> getActiveSessions() {
		// There is no distinction between active vs passive sessions
		return this.getSessions();
	}

	@Override
	public Set<String> getSessions() {
		try (Stream<Key<String>> keys = this.cache.withFlags(Flag.SKIP_LISTENER_NOTIFICATION).keySet().stream()) {
			return keys.filter(SessionCreationMetaDataKey.class::isInstance).map(Key::getId).collect(Collectors.toUnmodifiableSet());
		}
	}
}
