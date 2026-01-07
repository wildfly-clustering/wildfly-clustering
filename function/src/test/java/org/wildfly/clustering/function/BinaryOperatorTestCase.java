/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BiFunction}.
 * @author Paul Ferraro
 */
public class BinaryOperatorTestCase {
	private final UUID value1 = UUID.randomUUID();
	private final UUID value2 = UUID.randomUUID();
	private final UUID result = UUID.randomUUID();

	@Test
	public void test() {
		assertThat(BinaryOperator.of(null).apply(this.value1, this.value2)).isNull();
		assertThat(BinaryOperator.of(this.result).apply(this.value1, this.value2)).isSameAs(this.result);
		assertThat(BinaryOperator.of(null).apply(this.value1, this.value2)).isNull();
		assertThat(BinaryOperator.former().apply(this.value1, this.value2)).isSameAs(this.value1);
		assertThat(BinaryOperator.latter().apply(this.value1, this.value2)).isSameAs(this.value2);
	}

	@Test
	public void of() {
		BiConsumer<UUID, UUID> consumer = mock(BiConsumer.class);
		Supplier<UUID> supplier = mock(Supplier.class);
		doReturn(this.result).when(supplier).get();

		BinaryOperator<UUID> function = BinaryOperator.of(consumer, supplier);
		UUID result = function.apply(this.value1, this.value2);

		assertThat(result).isSameAs(this.result);

		verify(consumer, only()).accept(this.value1, this.value2);
		verify(supplier, only()).get();
	}

	@Test
	public void andThen() {
		UUID interrimResult = UUID.randomUUID();
		BinaryOperator<UUID> before = mock(BinaryOperator.class);
		UnaryOperator<UUID> after = mock(UnaryOperator.class);
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
		BinaryOperator<UUID> after = mock(BinaryOperator.class);
		UnaryOperator<UUID> before1 = mock(UnaryOperator.class);
		UnaryOperator<UUID> before2 = mock(UnaryOperator.class);
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
		BinaryOperator<UUID> after = mock(BinaryOperator.class);
		UnaryOperator<UUID> before1 = mock(UnaryOperator.class);
		UnaryOperator<UUID> before2 = mock(UnaryOperator.class);
		doCallRealMethod().when(after).composeUnary(any(), any());
		doReturn(interrimResult1).when(before1).apply(this.value1);
		doReturn(interrimResult2).when(before2).apply(this.value1);
		doReturn(this.result).when(after).apply(interrimResult1, interrimResult2);

		UUID result = after.composeUnary(before1, before2).apply(this.value1);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void reverse() {
		BinaryOperator<UUID> function = mock(BinaryOperator.class);
		doReturn(this.result).when(function).apply(this.value1, this.value2);
		doCallRealMethod().when(function).reverse();

		UUID result = function.reverse().apply(this.value2, this.value1);

		assertThat(result).isSameAs(this.result);
	}

	@Test
	public void orDefault() {
		BiPredicate<UUID, UUID> predicate = mock(BiPredicate.class);
		UUID otherwise = UUID.randomUUID();

		doReturn(false, true).when(predicate).test(this.value1, this.value2);

		BinaryOperator<UUID> function = BinaryOperator.when(predicate, BinaryOperator.of(this.result), BinaryOperator.of(otherwise));

		assertThat(function.apply(this.value1, this.value2)).isSameAs(otherwise);
		assertThat(function.apply(this.value1, this.value2)).isSameAs(this.result);
	}
}
