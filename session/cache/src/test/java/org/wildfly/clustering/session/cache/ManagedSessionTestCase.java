/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.junit.jupiter.api.Assertions.*;
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

		assertSame(attachedId, this.session.getId());

		verify(this.attachedSession).getId();
		verify(this.detachedSession, never()).getId();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertSame(detachedId, this.session.getId());

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

		assertSame(attachedContext, this.session.getContext());

		verify(this.attachedSession).getContext();
		verify(this.detachedSession, never()).getContext();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertSame(detachedContext, this.session.getContext());

		verifyNoMoreInteractions(this.attachedSession);

		this.session.close();

		verify(this.detachedSession).close();
		verifyNoMoreInteractions(this.attachedSession);
	}

	@Test
	public void isValid() {
		doReturn(true).when(this.attachedSession).isValid();
		doReturn(false).when(this.detachedSession).isValid();

		assertTrue(this.session.isValid());

		verify(this.attachedSession).isValid();
		verify(this.detachedSession, never()).isValid();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertFalse(this.session.isValid());

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

		assertSame(attachedMetaData, this.session.getMetaData());

		verify(this.attachedSession).getMetaData();
		verify(this.detachedSession, never()).getMetaData();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertSame(detachedMetaData, this.session.getMetaData());

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

		assertSame(attachedAttributes, this.session.getAttributes());

		verify(this.attachedSession).getAttributes();
		verify(this.detachedSession, never()).getAttributes();

		this.session.close();

		verify(this.attachedSession).close();
		verify(this.detachedSession, never()).close();

		assertSame(detachedAttributes, this.session.getAttributes());

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
