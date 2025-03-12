/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.SessionMetaData;

/**
 * Unit test for {@link ManagedSession}.
 * @author Paul Ferraro
 */
public class ManagedSessionTestCase {

	private final Session<String> attachedSession = mock(Session.class);
	private final Session<String> detachedSession = mock(Session.class);
	private final Session<String> session = new ManagedSession<>(this.attachedSession, this.detachedSession);

	@Test
	public void getId() {
		String attachedId = "attached";
		String detachedId = "detached";
		doReturn(attachedId).when(this.attachedSession).getId();
		doReturn(detachedId).when(this.detachedSession).getId();

		assertThat(this.session.getId()).isSameAs(attachedId);

		verify(this.attachedSession).getId();
		verify(this.detachedSession, never()).getId();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertThat(this.session.getId()).isSameAs(detachedId);

		verifyNoMoreInteractions(this.attachedSession);

		this.session.close();

		verify(this.detachedSession).close();
		verifyNoMoreInteractions(this.attachedSession);
	}

	@Test
	public void getContext() {
		String attachedContext = "attached";
		String detachedContext = "detached";
		doReturn(attachedContext).when(this.attachedSession).getContext();
		doReturn(detachedContext).when(this.detachedSession).getContext();

		assertThat(this.session.getContext()).isSameAs(attachedContext);

		verify(this.attachedSession).getContext();
		verify(this.detachedSession, never()).getContext();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertThat(this.session.getContext()).isSameAs(detachedContext);

		verifyNoMoreInteractions(this.attachedSession);

		this.session.close();

		verify(this.detachedSession).close();
		verifyNoMoreInteractions(this.attachedSession);
	}

	@Test
	public void isValid() {
		doReturn(true).when(this.attachedSession).isValid();
		doReturn(false).when(this.detachedSession).isValid();

		assertThat(this.session.isValid()).isTrue();

		verify(this.attachedSession).isValid();
		verify(this.detachedSession, never()).isValid();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertThat(this.session.isValid()).isFalse();

		verifyNoMoreInteractions(this.attachedSession);

		this.session.close();

		verify(this.detachedSession).close();
		verifyNoMoreInteractions(this.attachedSession);
	}

	@Test
	public void getMetaData() {
		SessionMetaData attachedMetaData = mock(SessionMetaData.class);
		SessionMetaData detachedMetaData = mock(SessionMetaData.class);
		doReturn(attachedMetaData).when(this.attachedSession).getMetaData();
		doReturn(detachedMetaData).when(this.detachedSession).getMetaData();

		assertThat(this.session.getMetaData()).isSameAs(attachedMetaData);

		verify(this.attachedSession).getMetaData();
		verify(this.detachedSession, never()).getMetaData();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertThat(this.session.getMetaData()).isSameAs(detachedMetaData);

		verifyNoMoreInteractions(this.attachedSession);

		this.session.close();

		verify(this.detachedSession).close();
		verifyNoMoreInteractions(this.attachedSession);
	}

	@Test
	public void getAttributes() {
		Map<String, Object> attachedAttributes = mock(Map.class);
		Map<String, Object> detachedAttributes = mock(Map.class);
		doReturn(attachedAttributes).when(this.attachedSession).getAttributes();
		doReturn(detachedAttributes).when(this.detachedSession).getAttributes();

		assertThat(this.session.getAttributes()).isSameAs(attachedAttributes);

		verify(this.attachedSession).getAttributes();
		verify(this.detachedSession, never()).getAttributes();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertThat(this.session.getAttributes()).isSameAs(detachedAttributes);

		verifyNoMoreInteractions(this.attachedSession);

		this.session.close();

		verify(this.detachedSession).close();
		verifyNoMoreInteractions(this.attachedSession);
	}

	@Test
	public void invalidate() {
		this.session.invalidate();

		verify(this.attachedSession).invalidate();
		verify(this.detachedSession, never()).invalidate();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		this.session.invalidate();

		verify(this.detachedSession).invalidate();
		verifyNoMoreInteractions(this.attachedSession);

		this.session.close();

		verify(this.detachedSession).close();
		verifyNoMoreInteractions(this.attachedSession);
	}
}
