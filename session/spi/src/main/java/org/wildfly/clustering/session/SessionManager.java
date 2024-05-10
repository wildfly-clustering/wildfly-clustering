/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.util.concurrent.CompletionStage;

import org.wildfly.clustering.server.manager.Manager;

/**
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public interface SessionManager<C> extends Manager<String> {

	/**
	 * Creates a session using the specified identifier.
	 * Sessions returned by this method must be closed via {@link Session#close()}.
	 * This method is intended to be invoked within the context of a batch.
	 * @param id a session identifier
	 * @return a new web session, or null if a session with the specified identifier already exists.
	 */
	default Session<C> createSession(String id) {
		return this.createSessionAsync(id).toCompletableFuture().join();
	}

	/**
	 * Creates a session using the specified identifier.
	 * Sessions returned by this method must be closed via {@link Session#close()}.
	 * This method is intended to be invoked within the context of a batch.
	 * @param id a session identifier
	 * @return a new web session, or null if a session with the specified identifier already exists.
	 */
	CompletionStage<Session<C>> createSessionAsync(String id);

	/**
	 * Returns the session with the specified identifier, or null if none exists.
	 * Sessions returned by this method must be closed via {@link Session#close()}.
	 * This method is intended to be invoked within the context of a batch.
	 * @param id a session identifier
	 * @return an existing web session, or null if none exists
	 */
	default Session<C> findSession(String id) {
		return this.findSessionAsync(id).toCompletableFuture().join();
	}

	/**
	 * Returns the session with the specified identifier, or null if none exists.
	 * Sessions returned by this method must be closed via {@link Session#close()}.
	 * This method is intended to be invoked within the context of a batch.
	 * @param id a session identifier
	 * @return an existing web session, or null if none exists
	 */
	CompletionStage<Session<C>> findSessionAsync(String id);

	/**
	 * Returns a read-only view of the session with the specified identifier.
	 * This method is intended to be invoked within the context of a batch
	 * @param id a unique session identifier
	 * @return a read-only session or null if none exists
	 */
	default ImmutableSession findImmutableSession(String id) {
		return this.findImmutableSessionAsync(id).toCompletableFuture().join();
	}

	/**
	 * Returns a read-only view of the session with the specified identifier.
	 * This method is intended to be invoked within the context of a batch
	 * @param id a unique session identifier
	 * @return a read-only session or null if none exists
	 */
	CompletionStage<ImmutableSession> findImmutableSessionAsync(String id);

	/**
	 * Returns a detached session with the specified identifier.
	 * A detached session is only valid if a session exists for the given identifier.
	 * @param id the session identifier of the detached session
	 * @return a detached session
	 */
	Session<C> getDetachedSession(String id);

	/**
	 * Returns statistics for this session manager.
	 * @return an object from which statistics can be obtained.
	 */
	SessionStatistics getStatistics();
}
