/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DetachableHttpSession}.
 * @author Paul Ferraro
 */
public class DetachableHttpSessionTestCase {

	private final HttpSession attached = mock(HttpSession.class);
	private final HttpSession detached = mock(HttpSession.class);
	private final HttpSession session = new DetachableHttpSession(this.attached, this.detached);
	private final Random random = new Random();

	@Test
	public void getServletContext() {
		ServletContext context = mock(ServletContext.class);

		doReturn(context).when(this.attached).getServletContext();
		doReturn(context).when(this.detached).getServletContext();

		assertThat(this.session.getServletContext()).isSameAs(context);
		assertThat(this.session.getServletContext()).isSameAs(context);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).getServletContext();
		verifyNoInteractions(this.detached);

		this.session.invalidate();

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).invalidate();

		assertThat(this.session.getServletContext()).isSameAs(context);
		assertThat(this.session.getServletContext()).isSameAs(context);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).getServletContext();
	}

	@Test
	public void getId() {
		String expected = "foo";

		doReturn(expected).when(this.attached).getId();
		doReturn(expected).when(this.detached).getId();

		assertThat(this.session.getId()).isSameAs(expected);
		assertThat(this.session.getId()).isSameAs(expected);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).getId();
		verifyNoInteractions(this.detached);

		this.session.invalidate();

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).invalidate();

		assertThat(this.session.getId()).isSameAs(expected);
		assertThat(this.session.getId()).isSameAs(expected);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).getId();
	}

	@Test
	public void getCreationTime() {
		long expected = System.currentTimeMillis();

		doReturn(expected).when(this.attached).getCreationTime();
		doReturn(expected).when(this.detached).getCreationTime();

		assertThat(this.session.getCreationTime()).isEqualTo(expected);
		assertThat(this.session.getCreationTime()).isEqualTo(expected);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).getCreationTime();
		verifyNoInteractions(this.detached);

		this.session.invalidate();

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).invalidate();

		assertThat(this.session.getCreationTime()).isEqualTo(expected);
		assertThat(this.session.getCreationTime()).isEqualTo(expected);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).getCreationTime();
	}

	@Test
	public void getMaxInactiveInterval() {
		int expected = this.random.nextInt(Short.MAX_VALUE);

		doReturn(expected).when(this.attached).getMaxInactiveInterval();
		doReturn(expected).when(this.detached).getMaxInactiveInterval();

		assertThat(this.session.getMaxInactiveInterval()).isEqualTo(expected);
		assertThat(this.session.getMaxInactiveInterval()).isEqualTo(expected);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).getMaxInactiveInterval();
		verifyNoInteractions(this.detached);

		this.session.setMaxInactiveInterval(expected);

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).setMaxInactiveInterval(expected);

		assertThat(this.session.getMaxInactiveInterval()).isEqualTo(expected);
		assertThat(this.session.getMaxInactiveInterval()).isEqualTo(expected);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).getMaxInactiveInterval();
	}

	@Test
	public void getLastAccessedTime() {
		long expected = System.currentTimeMillis();

		doReturn(expected).when(this.attached).getLastAccessedTime();
		doReturn(expected).when(this.detached).getLastAccessedTime();

		assertThat(this.session.getLastAccessedTime()).isEqualTo(expected);
		assertThat(this.session.getLastAccessedTime()).isEqualTo(expected);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).getLastAccessedTime();
		verifyNoInteractions(this.detached);

		this.session.invalidate();

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).invalidate();

		assertThat(this.session.getLastAccessedTime()).isEqualTo(expected);
		assertThat(this.session.getLastAccessedTime()).isEqualTo(expected);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).getLastAccessedTime();
	}

	@Test
	public void isNew() {
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(this.attached).isNew();
		doReturn(expected).when(this.detached).isNew();

		assertThat(this.session.isNew()).isEqualTo(expected);
		assertThat(this.session.isNew()).isEqualTo(expected);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).isNew();
		verifyNoInteractions(this.detached);

		this.session.invalidate();

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).invalidate();

		assertThat(this.session.isNew()).isEqualTo(expected);
		assertThat(this.session.isNew()).isEqualTo(expected);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).isNew();
	}

	@Test
	public void getAttributeNames() {
		Enumeration<String> expected = Collections.enumeration(List.of("foo"));

		doReturn(expected).when(this.attached).getAttributeNames();
		doReturn(expected).when(this.detached).getAttributeNames();

		assertThat(this.session.getAttributeNames()).isSameAs(expected);
		assertThat(this.session.getAttributeNames()).isSameAs(expected);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).getAttributeNames();
		verifyNoInteractions(this.detached);

		this.session.removeAttribute("foo");

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).removeAttribute("foo");

		assertThat(this.session.getAttributeNames()).isSameAs(expected);
		assertThat(this.session.getAttributeNames()).isSameAs(expected);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).getAttributeNames();
	}

	@Test
	public void getAttribute() {
		String name = "foo";
		Object expected = UUID.randomUUID();

		doReturn(expected).when(this.attached).getAttribute(name);
		doReturn(expected).when(this.detached).getAttribute(name);

		assertThat(this.session.getAttribute(name)).isSameAs(expected);
		assertThat(this.session.getAttribute(name)).isSameAs(expected);

		// Verify that read operation uses attached session until first write
		verify(this.attached, times(2)).getAttribute(name);
		verifyNoInteractions(this.detached);

		this.session.setAttribute(name, expected);

		verifyNoMoreInteractions(this.attached);
		verify(this.detached).setAttribute(name, expected);

		assertThat(this.session.getAttribute(name)).isSameAs(expected);
		assertThat(this.session.getAttribute(name)).isSameAs(expected);

		// Verify that, after write, read operations use detached session
		verifyNoMoreInteractions(this.attached);
		verify(this.detached, times(2)).getAttribute(name);
	}

	@Test
	public void setMaxInactiveInterval() {
		int expected = this.random.nextInt(Short.MAX_VALUE);

		this.session.setMaxInactiveInterval(expected);

		// Verify that write operations always use detached session
		verifyNoInteractions(this.attached);
		verify(this.detached).setMaxInactiveInterval(expected);

		this.session.setMaxInactiveInterval(expected);

		verifyNoInteractions(this.attached);
		verify(this.detached, times(2)).setMaxInactiveInterval(expected);
	}

	@Test
	public void setAttribute() {
		String name = "foo";
		Object value = UUID.randomUUID();

		this.session.setAttribute(name, value);

		// Verify that write operations always use detached session
		verifyNoInteractions(this.attached);
		verify(this.detached).setAttribute(name, value);

		this.session.setAttribute(name, value);

		verifyNoInteractions(this.attached);
		verify(this.detached, times(2)).setAttribute(name, value);
	}

	@Test
	public void removeAttribute() {
		String name = "foo";

		this.session.removeAttribute(name);

		// Verify that write operations always use detached session
		verifyNoInteractions(this.attached);
		verify(this.detached).removeAttribute(name);

		this.session.removeAttribute(name);

		verifyNoInteractions(this.attached);
		verify(this.detached, times(2)).removeAttribute(name);
	}

	@Test
	public void invalidate() {
		this.session.invalidate();

		// Verify that write operations always use detached session
		verifyNoInteractions(this.attached);
		verify(this.detached).invalidate();

		this.session.invalidate();

		verifyNoInteractions(this.attached);
		verify(this.detached, times(2)).invalidate();
	}
}
