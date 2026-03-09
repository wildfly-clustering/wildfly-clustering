/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;
import java.util.Map;

import javax.servlet.ServletContext;

import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.function.UnaryOperator;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * A mutable {@link javax.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class MutableHttpSession<C> extends ImmutableHttpSession {
	private static final UnaryOperator<Session<?>> REQUIRE_VALID = UnaryOperator.when(Session::isValid, UnaryOperator.identity(), UnaryOperator.of(Consumer.<Session<?>>of().thenThrow(IllegalStateException::new), Supplier.of(null)));
	private static final Function<Session<?>, SessionMetaData> METADATA = REQUIRE_VALID.thenApply(Session::getMetaData);
	private static final Function<Session<?>, Map<String, Object>> ATTRIBUTES = REQUIRE_VALID.thenApply(Session::getAttributes);
	private static final Consumer<Session<?>> INVALIDATE = REQUIRE_VALID.thenAccept(Session::invalidate);
	private static final BiConsumer<SessionMetaData, Duration> MAX_IDLE = SessionMetaData::setMaxIdle;
	private static final BiConsumer<Map<String, Object>, Map.Entry<String, Object>> SET_ATTRIBUTE = (map, entry) -> map.put(entry.getKey(), entry.getValue());
	private static final BiConsumer<Map<String, Object>, String> REMOVE_ATTRIBUTE = Map::remove;

	private final Reference.Reader<Session<C>> reader;
	private final Reference.Reader<SessionMetaData> metaDataReader;
	private final Reference.Reader<Map<String, Object>> attributesReader;

	MutableHttpSession(java.util.function.Supplier<String> identifier, Reference<Session<C>> reference, ServletContext context) {
		super(identifier, reference, context);
		this.reader = reference.getReader();
		this.attributesReader = this.reader.map(ATTRIBUTES);
		this.metaDataReader = this.reader.map(METADATA);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		Duration maxIdle = interval > 0 ? Duration.ofSeconds(interval) : Duration.ZERO;
		this.metaDataReader.read(MAX_IDLE.composeUnary(Function.identity(), Function.of(maxIdle)));
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.attributesReader.read(SET_ATTRIBUTE.composeUnary(Function.identity(), Function.of(Map.entry(name, value))));
	}

	@Override
	public void removeAttribute(String name) {
		this.attributesReader.read(REMOVE_ATTRIBUTE.composeUnary(Function.identity(), Function.of(name)));
	}

	@Override
	public void invalidate() {
		this.reader.read(INVALIDATE);
	}
}
