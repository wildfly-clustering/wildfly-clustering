/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.util.Reference;
import org.wildfly.clustering.session.Session;

/**
 * Unit test for {@link HttpSessionAccessor}.
 * @author Paul Ferraro
 */
public class HttpSessionAccessorTestCase {

	@Test
	public void test() {
		Session<Void> session = mock(Session.class);
		Reference<Session<Void>> reference = Reference.of(session);
		ServletContext context = mock(ServletContext.class);
		String id = "foo";

		HttpSession.Accessor accessor = new HttpSessionAccessor<>(reference, context);

		Consumer<HttpSession> reader = new Consumer<>() {
			@Override
			public void accept(HttpSession session) {
				assertThat(session.getId()).isSameAs(id);
				assertThat(session.getServletContext()).isSameAs(context);
			}
		};

		doReturn(id).when(session).getId();

		accessor.access(reader);
	}
}
