/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import java.util.Map;
import java.util.concurrent.CancellationException;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.cache.SessionFactory;

/**
 * Session remover that removes a session if and only if it is expired.
 * @param <SC> the ServletContext specification type
 * @param <MV> the meta-data value type
 * @param <AV> the attributes value type
 * @param <LC> the local context type
 * @author Paul Ferraro
 */
public class SessionExpirationTask<SC, MV, AV, LC> implements Predicate<String> {
	private static final System.Logger LOGGER = System.getLogger(SessionExpirationTask.class.getName());

	private final SessionFactory<SC, MV, AV, LC> sessionFactory;
	private final Supplier<Batch> batchFactory;
	private final Consumer<ImmutableSession> expirationListener;

	/**
	 * Creates a remover for expired sessions.
	 * @param sessionFactory the associated session factory
	 * @param batchFactory the batch factory
	 * @param expirationListener the listener of expired sessions
	 */
	public SessionExpirationTask(SessionFactory<SC, MV, AV, LC> sessionFactory, Supplier<Batch> batchFactory, Consumer<ImmutableSession> expirationListener) {
		this.sessionFactory = sessionFactory;
		this.batchFactory = batchFactory;
		this.expirationListener = expirationListener;
	}

	@Override
	public boolean test(String id) {
		LOGGER.log(System.Logger.Level.DEBUG, "Initiating expiration of session {0}", id);
		try (Batch batch = this.batchFactory.get()) {
			try {
				MV metaDataValue = this.sessionFactory.getSessionMetaDataFactory().tryValue(id);
				if (metaDataValue != null) {
					ImmutableSessionMetaData metaData = this.sessionFactory.getSessionMetaDataFactory().createImmutableSessionMetaData(id, metaDataValue);
					if (metaData.isExpired()) {
						LOGGER.log(System.Logger.Level.TRACE, "Removing expired session {0}.", id);
						AV attributesValue = this.sessionFactory.getSessionAttributesFactory().findValue(id);
						if (attributesValue != null) {
							Map<String, Object> attributes = this.sessionFactory.getSessionAttributesFactory().createImmutableSessionAttributes(id, attributesValue);
							ImmutableSession session = this.sessionFactory.createImmutableSession(id, metaData, attributes);
							this.expirationListener.accept(session);
						}
						try {
							this.sessionFactory.remove(id);
							return true;
						} catch (CancellationException e) {
							LOGGER.log(System.Logger.Level.TRACE, "Removal of session {0} was cancelled.", id);
							return false;
						}
					}
					LOGGER.log(System.Logger.Level.INFO, "Session {0} does not expire until {1}", id, metaData.getExpirationTime().orElse(null));
					return false;
				}
				LOGGER.log(System.Logger.Level.TRACE, "Session {0} was not found or is currently in use.", id);
				return true;
			} catch (RuntimeException | Error e) {
				batch.discard();
				throw e;
			}
		} catch (RuntimeException | Error e) {
			LOGGER.log(System.Logger.Level.WARNING, e.getLocalizedMessage(), e);
			return false;
		}
	}
}
