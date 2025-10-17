/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.remote;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.cache.infinispan.remote.RemoteCacheConfiguration;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.cache.AbstractSessionManager;
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
	private final RemoteCache<Key<String>, ?> cache;

	interface Configuration<C, MV, AV, SC> extends AbstractSessionManager.Configuration<C, MV, AV, SC> {
		@Override
		RemoteCacheConfiguration getCacheConfiguration();

		@Override
		default java.util.function.Consumer<ImmutableSession> getExpiredSessionHandler() {
			Consumer<String> remover = this.getSessionFactory()::removeAsync;
			return this.getExpirationListener().andThen(remover.compose(ImmutableSession::getId));
		}

		@Override
		default java.util.function.Consumer<ImmutableSession> getSessionCloseTask() {
			return Consumer.empty();
		}
	}

	/**
	 * Creates a session manager.
	 * @param configuration the configuration of this session manager
	 */
	public HotRodSessionManager(Configuration<C, MV, AV, SC> configuration) {
		super(configuration);
		this.cache = configuration.getCacheConfiguration().getCache();
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
