/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.Remover;
import org.wildfly.clustering.server.util.Supplied;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;
import org.wildfly.common.function.Functions;

/**
 * Unit test for {@link CompositeSession}.
 *
 * @author paul
 */
public class CompositeSessionTestCase {
	private final String id = "session";
	private final InvalidatableSessionMetaData metaData = mock(InvalidatableSessionMetaData.class);
	private final SessionAttributes attributes = mock(SessionAttributes.class);
	private final Remover<String> remover = mock(Remover.class);
	private final Object context = new Object();

	private final Session<Object> session = new CompositeSession<>(this.id, this.metaData, this.attributes, Supplied.simple(), Functions.constantSupplier(this.context), this.remover);

	@Test
	public void getId() {
		assertSame(this.id, this.session.getId());
	}

	@Test
	public void getAttributes() {
		assertSame(this.attributes, this.session.getAttributes());
	}

	@Test
	public void getMetaData() {
		assertSame(this.metaData, this.session.getMetaData());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void invalidate() {
		when(this.metaData.invalidate()).thenReturn(true);

		this.session.invalidate();

		verify(this.remover).remove(this.id);
		reset(this.remover);

		when(this.metaData.invalidate()).thenReturn(false);

		this.session.invalidate();

		verify(this.remover, never()).remove(this.id);
	}

	@Test
	public void isValid() {
		when(this.metaData.isValid()).thenReturn(true);

		assertTrue(this.session.isValid());

		when(this.metaData.isValid()).thenReturn(false);

		assertFalse(this.session.isValid());
	}

	@Test
	public void close() {
		when(this.metaData.isValid()).thenReturn(true);

		this.session.close();

		verify(this.attributes).close();
		verify(this.metaData).close();

		reset(this.metaData, this.attributes);

		// Verify that session is not mutated if invalid
		when(this.metaData.isValid()).thenReturn(false);

		this.session.close();

		verify(this.attributes, never()).close();
		verify(this.metaData, never()).close();
	}

	@Test
	public void getLocalContext() {
		Object result = this.session.getContext();

		assertSame(this.context, result);
	}
}
