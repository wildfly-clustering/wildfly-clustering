/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BooleanSupplier}.
 * @author Paul Ferraro
 */
public class BooleanSupplierTestCase {

	@Test
	public void of() {
		assertThat(BooleanSupplier.of(false).getAsBoolean()).isEqualTo(false);
		assertThat(BooleanSupplier.of(true).getAsBoolean()).isEqualTo(true);

		assertThat(BooleanSupplier.TRUE.getAsBoolean()).isTrue();
		assertThat(BooleanSupplier.FALSE.getAsBoolean()).isFalse();
		assertThat(BooleanSupplier.TRUE.negate().getAsBoolean()).isFalse();
		assertThat(BooleanSupplier.FALSE.negate().getAsBoolean()).isTrue();
	}

	@Test
	public void handle() {
		this.handle(false, false);
		this.handle(false, true);
		this.handle(true, false);
		this.handle(true, true);
	}

	private void handle(boolean value, boolean handled) {
		BooleanSupplier supplier = mock(BooleanSupplier.class);

		doCallRealMethod().when(supplier).handle(any());
		doReturn(value).when(supplier).getAsBoolean();

		Predicate<RuntimeException> handler = mock(Predicate.class);
		RuntimeException exception = new RuntimeException();

		assertThat(supplier.handle(handler).getAsBoolean()).isEqualTo(value);

		verify(supplier).getAsBoolean();
		verify(handler, never()).test(any());

		doThrow(exception).when(supplier).getAsBoolean();
		doReturn(handled).when(handler).test(exception);

		assertThat(supplier.handle(handler).getAsBoolean()).isEqualTo(handled);

		verify(supplier, times(2)).getAsBoolean();
		verify(handler).test(any());
	}
}
