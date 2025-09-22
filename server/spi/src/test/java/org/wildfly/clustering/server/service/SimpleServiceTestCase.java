/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.function.Supplier;

/**
 * Unit test for {@link SimpleService}.
 * @author Paul Ferraro
 */
public class SimpleServiceTestCase {

	@Test
	public void test() {
		Supplier<AutoCloseable> factory = mock(Supplier.class);
		AutoCloseable value1 = mock(AutoCloseable.class);
		AutoCloseable value2 = mock(AutoCloseable.class);
		AtomicReference<AutoCloseable> reference = new AtomicReference<>();

		doReturn(value1, value2, null).when(factory).get();

		Service service = new SimpleService<>(factory, reference);

		verifyNoInteractions(factory);

		assertThat(service.isStarted()).isFalse();

		service.start();

		verify(factory).get();

		assertThat(reference).hasValue(value1);
		assertThat(service.isStarted()).isTrue();

		service.stop();

		verifyNoMoreInteractions(factory);

		assertThat(reference).hasNullValue();
		assertThat(service.isStarted()).isFalse();

		service.start();

		verify(factory, times(2)).get();

		assertThat(reference).hasValue(value2);
		assertThat(service.isStarted()).isTrue();

		// Redundant start
		service.start();

		verifyNoMoreInteractions(factory);

		assertThat(reference).hasValue(value2);
		assertThat(service.isStarted()).isTrue();

		service.stop();

		verifyNoMoreInteractions(factory);

		assertThat(reference).hasNullValue();
		assertThat(service.isStarted()).isFalse();

		// Redundant stop
		service.stop();

		verifyNoMoreInteractions(factory);

		assertThat(reference).hasNullValue();
		assertThat(service.isStarted()).isFalse();

		// Factory returns null
		service.start();

		verify(factory, times(3)).get();

		assertThat(reference).hasNullValue();
		assertThat(service.isStarted()).isFalse();

		service.stop();

		verifyNoMoreInteractions(factory);

		assertThat(reference).hasNullValue();
		assertThat(service.isStarted()).isFalse();
	}
}
