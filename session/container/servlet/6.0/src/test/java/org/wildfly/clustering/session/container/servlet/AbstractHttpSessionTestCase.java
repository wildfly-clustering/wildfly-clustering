/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * @author Paul Ferraro
 * @param <S> session type
 * @param <M> session metadata type
 */
public abstract class AbstractHttpSessionTestCase<S extends ImmutableSession, M extends ImmutableSessionMetaData> {

	final S session;
	final M metaData;
	final ServletContext context;
	final HttpSession subject;
	final Random random = new Random();

	AbstractHttpSessionTestCase(Class<S> sessionClass, Class<M> metaDataClass, BiFunction<S, ServletContext, HttpSession> factory) {
		this.session = mock(sessionClass);
		this.metaData = mock(metaDataClass);
		this.context = mock(ServletContext.class);
		this.subject = factory.apply(this.session, this.context);
	}

	@Test
	public void getServletContext() {
		assertThat(this.subject.getServletContext()).isSameAs(this.context);
	}

	@Test
	public void getId() {
		String expected = "foo";

		doReturn(expected).when(this.session).getId();

		assertThat(this.subject.getId()).isSameAs(expected);
	}

	@Test
	public void getCreationTime() {
		Instant now = Instant.now();

		doReturn(this.metaData).when(this.session).getMetaData();
		doReturn(now).when(this.metaData).getCreationTime();

		assertThat(this.subject.getCreationTime()).isEqualTo(now.toEpochMilli());
	}

	@Test
	public void getMaxInactiveInterval() {
		Duration timeout = Duration.ofSeconds(this.random.nextInt(Short.MAX_VALUE));

		doReturn(this.metaData).when(this.session).getMetaData();
		doReturn(timeout).when(this.metaData).getTimeout();

		assertThat(this.subject.getMaxInactiveInterval()).isEqualTo(timeout.getSeconds());
	}

	@Test
	public void getLastAccessedTime() {
		Instant now = Instant.now();

		doReturn(this.metaData).when(this.session).getMetaData();
		doReturn(now).when(this.metaData).getLastAccessStartTime();

		assertThat(this.subject.getLastAccessedTime()).isEqualTo(now.toEpochMilli());
	}

	@Test
	public void isNew() {
		boolean expected = this.random.nextBoolean();

		doReturn(this.metaData).when(this.session).getMetaData();
		doReturn(expected).when(this.metaData).isNew();

		assertThat(this.subject.isNew()).isEqualTo(expected);
	}

	@Test
	public void getAttributeNames() {
		Map<String, Object> attributes = Map.of("foo", UUID.randomUUID(), "bar", UUID.randomUUID());

		doReturn(attributes).when(this.session).getAttributes();

		assertThat(Collections.list(this.subject.getAttributeNames())).containsExactlyInAnyOrderElementsOf(attributes.keySet());
	}

	@Test
	public void getAttribute() {
		Map<String, Object> attributes = Map.of("foo", UUID.randomUUID(), "bar", UUID.randomUUID());

		doReturn(attributes).when(this.session).getAttributes();

		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			assertThat(this.subject.getAttribute(entry.getKey())).isSameAs(entry.getValue());
		}
		assertThat(this.subject.getAttribute("baz")).isNull();
	}

	@Test
	public void setMaxInactiveInterval() {
		this.subject.setMaxInactiveInterval(this.random.nextInt(Short.MAX_VALUE));

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}

	@Test
	public void setAttribute() {
		this.subject.setAttribute("foo", UUID.randomUUID());

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}

	@Test
	public void removeAttribute() {
		this.subject.removeAttribute("foo");

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}

	@Test
	public void invalidate() {
		this.subject.invalidate();

		verifyNoInteractions(this.session);
		verifyNoInteractions(this.context);
	}
}
