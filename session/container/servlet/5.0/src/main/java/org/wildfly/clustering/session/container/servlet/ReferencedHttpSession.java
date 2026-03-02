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

import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * A container session facade to a session reference.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class ReferencedHttpSession<C> extends AbstractHttpSession {
	private static final Function<Set<String>, Set<String>> COPY_SET = Set::copyOf;
	private static final Function<Session<?>, SessionMetaData> METADATA = Session::getMetaData;
	private static final Function<Session<?>, Map<String, Object>> ATTRIBUTES = Session::getAttributes;
	private static final Consumer<Session<?>> INVALIDATE = Session::invalidate;
	private static final Function<SessionMetaData, Instant> CREATION_TIME = SessionMetaData::getCreationTime;
	private static final Function<SessionMetaData, Optional<Instant>> LAST_ACCESS_TIME = SessionMetaData::getLastAccessStartTime;
	private static final Function<SessionMetaData, Map.Entry<Instant, Optional<Instant>>> CREATION_LAST_ACCESS_TIME = Function.entry(CREATION_TIME, LAST_ACCESS_TIME);
	private static final Function<SessionMetaData, Optional<Duration>> MAX_IDLE = SessionMetaData::getMaxIdle;
	private static final BiConsumer<SessionMetaData, Duration> SET_MAX_IDLE = SessionMetaData::setMaxIdle;
	private static final BiFunction<Map<String, Object>, String, Object> GET_ATTRIBUTE = Map::get;
	private static final BiFunction<Map<String, Object>, String, Object> REMOVE_ATTRIBUTE = Map::remove;
	private static final Function<Map<String, Object>, Set<String>> ATTRIBUTE_NAMES = Function.of(Map::keySet, COPY_SET);

	private final String id;
	private final Reference<Session<C>> reference;
	private final Reference.Reader<SessionMetaData> metaDataReader;
	private final Reference.Reader<Map<String, Object>> attributesReader;

	/**
	 * Constructs a new detached {@link jakarta.servlet.http.HttpSession}.
	 * @param reference a session reference
	 * @param id the id of the referenced session
	 * @param context the servlet context associated with the session
	 */
	public ReferencedHttpSession(Reference<Session<C>> reference, String id, ServletContext context) {
		super(context);
		this.reference = reference;
		this.id = id;
		this.metaDataReader = reference.getReader().map(METADATA);
		this.attributesReader = reference.getReader().map(ATTRIBUTES);
	}

	@Override
	public String getId() {
		return this.id;
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
		Map.Entry<Instant, Optional<Instant>> entry = this.metaDataReader.map(CREATION_LAST_ACCESS_TIME).get();
		return entry.getValue().orElse(entry.getKey()).toEpochMilli();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		Duration maxIdle = interval > 0 ? Duration.ofSeconds(interval) : Duration.ZERO;
		this.metaDataReader.read(SET_MAX_IDLE.composeUnary(Function.identity(), Function.of(maxIdle)));
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
	public void setAttribute(String name, Object value) {
		this.attributesReader.read(map -> map.put(name, value));
	}

	@Override
	public void removeAttribute(String name) {
		this.attributesReader.map(REMOVE_ATTRIBUTE.composeUnary(Function.identity(), Function.of(name)));
	}

	@Override
	public void invalidate() {
		this.reference.getReader().read(INVALIDATE);
	}
}
