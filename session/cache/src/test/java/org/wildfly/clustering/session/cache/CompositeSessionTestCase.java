/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.CacheEntryRemover;
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
	private final CacheEntryRemover<String> remover = mock(CacheEntryRemover.class);
	private final Object context = new Object();

	private final Session<Object> session = new CompositeSession<>(this.id, this.metaData, this.attributes, Supplied.simple(), Functions.constantSupplier(this.context), this.remover);

	@Test
	public void getId() {
		assertThat(this.session.getId()).isSameAs(this.id);
	}

	@Test
	public void getAttributes() {
		assertThat(this.session.getAttributes()).isSameAs(this.attributes);
	}

	@Test
	public void getMetaData() {
		assertThat(this.session.getMetaData()).isSameAs(this.metaData);
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

		assertThat(this.session.isValid()).isTrue();

		when(this.metaData.isValid()).thenReturn(false);

		assertThat(this.session.isValid()).isFalse();
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

		assertThat(result).isSameAs(this.context);
	}
}
