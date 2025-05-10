/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Unit test for {@link Supplier}.
 * @author Paul Ferraro
 */
public class SupplierTestCase {

	@Test
	public void map() {
		Object interrim = new Object();
		Object expected = new Object();
		Supplier<Object> supplier = Mockito.mock(Supplier.class);
		Mockito.doCallRealMethod().when(supplier).map(ArgumentMatchers.any());
		Mockito.doReturn(interrim).when(supplier).get();
		Function<Object, Object> mapper = Mockito.mock(Function.class);
		Mockito.doReturn(expected).when(mapper).apply(interrim);

		Object result = supplier.map(mapper).get();

		Assertions.assertThat(result).isSameAs(expected);
	}

	@Test
	public void ofValue() {
		Object expected = new Object();
		Assertions.assertThat(Supplier.of(expected).get()).isSameAs(expected);
		Assertions.assertThat(Supplier.of(null).get()).isNull();
	}

	@Test
	public void ofRunnable() {
		Runnable runner = Mockito.mock(Runnable.class);
		Assertions.assertThat(Supplier.of(runner).get()).isNull();
		Mockito.verify(runner).run();
		runner = null;
		Assertions.assertThat(Supplier.of(runner).get()).isNull();
	}
}
