/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.ServletContext;

import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * An immutable {@link jakarta.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 */
public class ImmutableHttpSession extends AbstractHttpSession {
	private static final UnaryOperator<ImmutableSession> REQUIRE_VALID = UnaryOperator.when(ImmutableSession::isValid, UnaryOperator.identity(), UnaryOperator.of(Consumer.<ImmutableSession>of().thenThrow(IllegalStateException::new), Supplier.of(null)));
	private static final Function<Set<String>, Set<String>> COPY_SET = Set::copyOf;
	private static final Function<ImmutableSession, ImmutableSessionMetaData> METADATA = REQUIRE_VALID.thenApply(ImmutableSession::getMetaData);
	private static final Function<ImmutableSession, Map<String, Object>> ATTRIBUTES = REQUIRE_VALID.thenApply(ImmutableSession::getAttributes);
	private static final Function<ImmutableSessionMetaData, Instant> CREATION_TIME = ImmutableSessionMetaData::getCreationTime;
	private static final Function<ImmutableSessionMetaData, Optional<Instant>> LAST_ACCESS_TIME = ImmutableSessionMetaData::getLastAccessStartTime;
	private static final Function<ImmutableSessionMetaData, Map.Entry<Instant, Optional<Instant>>> CREATION_LAST_ACCESS_TIME = Function.entry(CREATION_TIME, LAST_ACCESS_TIME);
	private static final Function<ImmutableSessionMetaData, Optional<Duration>> MAX_IDLE = ImmutableSessionMetaData::getMaxIdle;
	private static final BiFunction<Map<String, Object>, String, Object> GET_ATTRIBUTE = Map::get;
	private static final Function<Map<String, Object>, Set<String>> ATTRIBUTE_NAMES = Function.of(Map::keySet, COPY_SET);

	private final java.util.function.Supplier<String> identifier;
	private final Reference.Reader<ImmutableSessionMetaData> metaDataReader;
	private final Reference.Reader<Map<String, Object>> attributesReader;

	ImmutableHttpSession(java.util.function.Supplier<String> identifier, Reference<? extends ImmutableSession> reference, ServletContext context) {
		super(context);
		this.identifier = identifier;
		this.metaDataReader = reference.getReader().map(METADATA);
		this.attributesReader = reference.getReader().map(ATTRIBUTES);
	}

	@Override
	public String getId() {
		return this.identifier.get();
	}

	@Override
	public boolean isNew() {
		return this.metaDataReader.map(LAST_ACCESS_TIME).get().isEmpty();
	}

	@Override
	public long getCreationTime() {
		return this.metaDataReader.map(CREATION_TIME).get().toEpochMilli();
	}

	@Override
	public long getLastAccessedTime() {
		// Specification does not clearly define what this method should return for new sessions
		// Per Tomcat, default to creation time for new session
		Map.Entry<Instant, Optional<Instant>> entry = this.metaDataReader.map(CREATION_LAST_ACCESS_TIME).get();
		return entry.getValue().orElse(entry.getKey()).toEpochMilli();
	}

	@Override
	public int getMaxInactiveInterval() {
		return (int) this.metaDataReader.map(MAX_IDLE).get().orElse(Duration.ZERO).getSeconds();
	}

	@Override
	public Object getAttribute(String name) {
		return this.attributesReader.map(GET_ATTRIBUTE.composeUnary(Function.identity(), Function.of(name))).get();
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this.attributesReader.map(ATTRIBUTE_NAMES).get());
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		throw new IllegalStateException();
	}

	@Override
	public void setAttribute(String name, Object value) {
		throw new IllegalStateException();
	}

	@Override
	public void removeAttribute(String name) {
		throw new IllegalStateException();
	}

	@Override
	public void invalidate() {
		throw new IllegalStateException();
	}
}
