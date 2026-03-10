/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.util.Map;

import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * Represents a session.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public interface Session<C> extends ImmutableSession, AutoCloseable {
	/** An validation function that throwing an exception for invalid sessions. */
	UnaryOperator<Session<?>> REQUIRE_VALID = UnaryOperator.when(VALID, UnaryOperator.identity(), UnaryOperator.of(Consumer.<Session<?>>of().thenThrow(IllegalStateException::new), Supplier.of(null)));
	/** A function returning the valid metadata of a session. */
	Function<Session<?>, SessionMetaData> METADATA = REQUIRE_VALID.thenApply(Session::getMetaData);
	/** A function returning the valid attributes of a session. */
	Function<Session<?>, Map<String, Object>> ATTRIBUTES = ImmutableSession.ATTRIBUTES.compose(Function.identity());
	/** A consumer that invalidates a session. */
	Consumer<Session<?>> INVALIDATE = Session::invalidate;
	/** A function that sets an attribute of session. */
	BiFunction<Map<String, Object>, Map.Entry<String, Object>, Object> SET_ATTRIBUTE = new BiFunction<>() {
		@Override
		public Object apply(Map<String, Object> map, Map.Entry<String, Object> entry) {
			return map.put(entry.getKey(), entry.getValue());
		}
	};
	/** A function that removes an attribute of session. */
	BiFunction<Map<String, Object>, String, Object> REMOVE_ATTRIBUTE = Map::remove;

	@Override
	SessionMetaData getMetaData();

	/**
	 * Invalidates this session.
	 */
	void invalidate();

	/**
	 * Returns the local context of this session.
	 * The local context is *not* replicated to other nodes in the cluster.
	 * @return a local context
	 */
	C getContext();

	@Override
	void close();
}
