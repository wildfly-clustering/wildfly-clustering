/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BiFunction}.
 * @author Paul Ferraro
 */
public class BiFunctionTestCase {
	private final UUID value1 = UUID.randomUUID();
	private final UUID value2 = UUID.randomUUID();
	private final UUID result = UUID.randomUUID();

	@Test
	public void test() {
		assertThat(BiFunction.of(null).apply(this.value1, this.value2)).isNull();
		assertThat(BiFunction.of(this.result).apply(this.value1, this.value2)).isSameAs(this.result);
		assertThat(BiFunction.of(null).apply(this.value1, this.value2)).isNull();
		assertThat(BiFunction.former().apply(this.value1, this.value2)).isSameAs(this.value1);
		assertThat(BiFunction.latter().apply(this.value1, this.value2)).isSameAs(this.value2);
	}

	@Test
	public void of() {
		BiConsumer<UUID, UUID> consumer = mock(BiConsumer.class);
		Supplier<UUID> supplier = mock(Supplier.class);
		doReturn(this.result).when(supplier).get();

		BiFunction<UUID, UUID, UUID> function = BiFunction.of(consumer, supplier);
		UUID result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);

		verify(consumer, only()).accept(this.value1, this.value2);
		verify(supplier, only()).get();
	}

	@Test
	public void andThen() {
		UUID interrimResult = UUID.randomUUID();
		BiFunction<UUID, UUID, UUID> before = mock(BiFunction.class);
		Function<UUID, UUID> after = mock(Function.class);
		doCallRealMethod().when(before).andThen(any());
		doReturn(interrimResult).when(before).apply(this.value1, this.value2);
		doReturn(this.result).when(after).apply(interrimResult);

		Object result = before.andThen(after).apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void compose() {
		UUID interrimResult1 = UUID.randomUUID();
		UUID interrimResult2 = UUID.randomUUID();
		BiFunction<UUID, UUID, UUID> after = mock(BiFunction.class);
		Function<UUID, UUID> before1 = mock(Function.class);
		Function<UUID, UUID> before2 = mock(Function.class);
		doCallRealMethod().when(after).compose(any(), any());
		doReturn(interrimResult1).when(before1).apply(this.value1);
		doReturn(interrimResult2).when(before2).apply(this.value2);
		doReturn(this.result).when(after).apply(interrimResult1, interrimResult2);

		UUID result = after.compose(before1, before2).apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void composeUnary() {
		UUID interrimResult1 = UUID.randomUUID();
		UUID interrimResult2 = UUID.randomUUID();
		BiFunction<UUID, UUID, UUID> after = mock(BiFunction.class);
		Function<UUID, UUID> before1 = mock(Function.class);
		Function<UUID, UUID> before2 = mock(Function.class);
		doCallRealMethod().when(after).composeUnary(any(), any());
		doReturn(interrimResult1).when(before1).apply(this.value1);
		doReturn(interrimResult2).when(before2).apply(this.value1);
		doReturn(this.result).when(after).apply(interrimResult1, interrimResult2);

		UUID result = after.composeUnary(before1, before2).apply(this.value1);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void reverse() {
		BiFunction<UUID, UUID, UUID> function = mock(BiFunction.class);
		doReturn(this.result).when(function).apply(this.value1, this.value2);
		doCallRealMethod().when(function).reverse();

		UUID result = function.reverse().apply(this.value2, this.value1);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void when() {
		BiPredicate<UUID, UUID> predicate = mock(BiPredicate.class);
		UUID otherwise = UUID.randomUUID();

		doReturn(false, true).when(predicate).test(this.value1, this.value2);

		BiFunction<UUID, UUID, UUID> function = BiFunction.when(predicate, BiFunction.of(this.result), BiFunction.of(otherwise));

		assertThat(function.apply(this.value1, this.value2)).isSameAs(otherwise);
		assertThat(function.apply(this.value1, this.value2)).isSameAs(this.result);
	}
}
