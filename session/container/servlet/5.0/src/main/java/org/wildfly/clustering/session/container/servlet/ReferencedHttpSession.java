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
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.ServletContext;

import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * A container session facade to a session reference.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class ReferencedHttpSession<C> extends AbstractHttpSession {
	private final UnaryOperator<Session<C>> session = UnaryOperator.when(Objects::nonNull, UnaryOperator.identity(), UnaryOperator.of(Consumer.<Session<C>>of().thenThrow(IllegalStateException::new), Supplier.of(null)));
	private final Function<Session<C>, SessionMetaData> metaData = this.session.thenApply(Session::getMetaData);
	private final Function<Session<C>, Map<String, Object>> attributes = this.session.thenApply(Session::getAttributes);

	private final String id;
	private final Reference<Session<C>> reference;

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
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public boolean isNew() {
		return this.reference.getReader().read(this.metaData.thenApply(SessionMetaData::getLastAccessTime)).isEmpty();
	}

	@Override
	public long getCreationTime() {
		return this.reference.getReader().read(this.metaData.thenApply(SessionMetaData::getCreationTime)).toEpochMilli();
	}

	@Override
	public long getLastAccessedTime() {
		Map.Entry<Optional<Instant>, Instant> entry = this.reference.getReader().read(this.metaData.thenApply(Function.entry(SessionMetaData::getLastAccessStartTime, SessionMetaData::getCreationTime)));
		return entry.getKey().orElse(entry.getValue()).toEpochMilli();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.reference.getReader().consume(this.metaData.thenAccept(metaData -> metaData.setMaxIdle(Duration.ofSeconds(interval))));
	}

	@Override
	public int getMaxInactiveInterval() {
		return (int) this.reference.getReader().read(this.metaData.thenApply(SessionMetaData::getMaxIdle)).orElse(Duration.ZERO).getSeconds();
	}

	@Override
	public Object getAttribute(String name) {
		return this.reference.getReader().read(this.attributes.thenApply(map -> map.get(name)));
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this.reference.getReader().read(this.attributes.thenApply(Map::keySet)));
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.reference.getReader().consume(this.attributes.thenAccept(map -> map.put(name, value)));
	}

	@Override
	public void removeAttribute(String name) {
		this.reference.getReader().consume(this.attributes.thenAccept(map -> map.remove(name)));
	}

	@Override
	public void invalidate() {
		this.reference.getReader().consume(this.session.thenAccept(Session::invalidate));
	}
}
