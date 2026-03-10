/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.servlet.ServletContext;

import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * An immutable {@link jakarta.servlet.http.HttpSession} facade.
 * @author Paul Ferraro
 * @param <S> the session type
 */
public class ImmutableHttpSession<S extends ImmutableSession> extends AbstractHttpSession {
	private final Reference.Reader<ImmutableSessionMetaData> metaDataReader;
	private final Reference.Reader<Map<String, Object>> attributesReader;

	ImmutableHttpSession(Supplier<String> identifier, Reference<S> reference, ServletContext context, java.util.function.Function<String, Accessor> accessorFactory) {
		super(identifier, context, accessorFactory);
		this.metaDataReader = reference.getReader().map(ImmutableSession.METADATA);
		this.attributesReader = reference.getReader().map(ImmutableSession.ATTRIBUTES);
	}

	@Override
	public boolean isNew() {
		return this.metaDataReader.map(ImmutableSessionMetaData.LAST_ACCESS_START_TIME).get().isEmpty();
	}

	@Override
	public long getCreationTime() {
		return this.metaDataReader.map(ImmutableSessionMetaData.CREATION_TIME).get().toEpochMilli();
	}

	@Override
	public long getLastAccessedTime() {
		return this.metaDataReader.map(ImmutableSessionMetaData.LAST_ACCESS_TIME).get().toEpochMilli();
	}

	@Override
	public int getMaxInactiveInterval() {
		return (int) this.metaDataReader.map(ImmutableSessionMetaData.MAX_IDLE).get().orElse(Duration.ZERO).getSeconds();
	}

	@Override
	public Object getAttribute(String name) {
		return this.attributesReader.map(ImmutableSession.GET_ATTRIBUTE.composeUnary(Function.identity(), Function.of(name))).get();
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this.attributesReader.map(ImmutableSession.ATTRIBUTE_NAMES).get());
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
