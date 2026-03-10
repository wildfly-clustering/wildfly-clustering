/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session;

import java.util.Map;
import java.util.Set;

import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * An immutable view of a session.
 * @author Paul Ferraro
 */
public interface ImmutableSession {
	/** A function returning the identifier of a session. */
	Function<ImmutableSession, String> IDENTIFIER = ImmutableSession::getId;
	/** A predicate indicating whether a given session is valid. */
	Predicate<ImmutableSession> VALID = ImmutableSession::isValid;
	/** An validation function that throwing an exception for invalid sessions. */
	UnaryOperator<ImmutableSession> REQUIRE_VALID = UnaryOperator.when(VALID, UnaryOperator.identity(), UnaryOperator.of(Consumer.<ImmutableSession>of().thenThrow(IllegalStateException::new), Supplier.of(null)));
	/** A function returning the valid metadata of a session */
	Function<ImmutableSession, ImmutableSessionMetaData> METADATA = REQUIRE_VALID.thenApply(ImmutableSession::getMetaData);
	/** A function returning the valid attributes of a session */
	Function<ImmutableSession, Map<String, Object>> ATTRIBUTES = REQUIRE_VALID.thenApply(ImmutableSession::getAttributes);
	/** A function returning an attribute of a session */
	BiFunction<Map<String, Object>, String, Object> GET_ATTRIBUTE = Map::get;
	/** A function returning the attribute names of a session */
	Function<Map<String, Object>, Set<String>> ATTRIBUTE_NAMES = UnaryOperator.<Map<String, Object>>identity().thenApply(Map::keySet).thenApply(Set::copyOf);

	/**
	 * Specifies this session's unique identifier.
	 * @return a unique identifier for this session.
	 */
	String getId();

	/**
	 * Indicates whether or not this session is valid.
	 * @return true, if this session is valid, false otherwise
	 */
	boolean isValid();

	/**
	 * Returns this session's meta data.
	 * @return this session's meta data
	 */
	ImmutableSessionMetaData getMetaData();

	/**
	 * Returns this session's attributes.
	 * @return this session's attributes
	 */
	Map<String, Object> getAttributes();
}
