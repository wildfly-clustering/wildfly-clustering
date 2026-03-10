/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.ServletContext;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * A mutable {@link javax.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public class MutableHttpSession<C> extends ImmutableHttpSession<Session<C>> {

	private final Reference.Reader<Session<C>> reader;
	private final Reference.Reader<SessionMetaData> metaDataReader;
	private final Reference.Reader<Map<String, Object>> attributesReader;

	MutableHttpSession(Supplier<String> identifier, Reference<Session<C>> reference, ServletContext context) {
		super(identifier, reference, context);
		this.reader = reference.getReader();
		this.attributesReader = this.reader.map(ImmutableSession.ATTRIBUTES);
		this.metaDataReader = this.reader.map(Session.METADATA);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		Duration maxIdle = interval > 0 ? Duration.ofSeconds(interval) : Duration.ZERO;
		this.metaDataReader.read(SessionMetaData.MAX_IDLE.composeUnary(Function.identity(), Function.of(maxIdle)));
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.attributesReader.map(Session.SET_ATTRIBUTE.composeUnary(Function.identity(), Function.of(Map.entry(name, value)))).get();
	}

	@Override
	public void removeAttribute(String name) {
		this.attributesReader.map(Session.REMOVE_ATTRIBUTE.composeUnary(Function.identity(), Function.of(name))).get();
	}

	@Override
	public void invalidate() {
		this.reader.read(Session.INVALIDATE);
	}
}
