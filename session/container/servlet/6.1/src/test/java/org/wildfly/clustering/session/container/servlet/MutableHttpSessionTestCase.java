/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.mockito.ArgumentCaptor;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * Unit test for {@link MutableHttpSession}.
 * @author Paul Ferraro
 */
public class MutableHttpSessionTestCase extends AbstractHttpSessionTestCase<Session<Void>, SessionMetaData> {

	@SuppressWarnings("unchecked")
	public MutableHttpSessionTestCase() {
		super((Class<Session<Void>>) (Class<?>) Session.class, SessionMetaData.class, MutableHttpSession::new);
	}

	@Override
	public void setMaxInactiveInterval() {
		int timeout = this.random.nextInt(Short.MAX_VALUE);
		ArgumentCaptor<Duration> capturedTimeout = ArgumentCaptor.forClass(Duration.class);

		doReturn(this.metaData).when(this.session).getMetaData();

		this.subject.setMaxInactiveInterval(timeout);

		verify(this.metaData).setMaxIdle(capturedTimeout.capture());

		assertThat(capturedTimeout.getValue()).hasSeconds(timeout);
	}

	@Override
	public void setAttribute() {
		String key = "foo";
		Object value = UUID.randomUUID();
		Map<String, Object> attributes = mock(Map.class);

		doReturn(attributes).when(this.session).getAttributes();

		this.subject.setAttribute(key, value);

		verify(attributes).put(key, value);
	}

	@Override
	public void removeAttribute() {
		String key = "foo";
		Map<String, Object> attributes = mock(Map.class);

		doReturn(attributes).when(this.session).getAttributes();

		this.subject.removeAttribute(key);

		verify(attributes).remove(key);
	}

	@Override
	public void invalidate() {
		this.subject.invalidate();

		verify(this.session).invalidate();
	}
}
