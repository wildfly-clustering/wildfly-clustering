/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Unit test for {@link Function}.
 * @author Paul Ferraro
 */
public class UnaryOperatorTestCase {

	@Test
	public void compose() {
		UnaryOperator<UUID> function = mock(UnaryOperator.class);
		doCallRealMethod().when(function).compose(ArgumentMatchers.<UnaryOperator<UUID>>any());
		UnaryOperator<UUID> mapper = mock(UnaryOperator.class);
		UUID value = UUID.randomUUID();
		UUID mapped = UUID.randomUUID();
		UUID expected = UUID.randomUUID();
		doReturn(mapped).when(mapper).apply(value);
		doReturn(expected).when(function).apply(mapped);

		assertThat(function.compose(mapper).apply(value)).isSameAs(expected);

		verify(function).apply(mapped);
	}

	@Test
	public void composeBinary() {
		UnaryOperator<UUID> function = mock(UnaryOperator.class);
		doCallRealMethod().when(function).compose(ArgumentMatchers.<BinaryOperator<UUID>>any());
		BinaryOperator<UUID> mapper = mock(BinaryOperator.class);
		UUID value1 = UUID.randomUUID();
		UUID value2 = UUID.randomUUID();
		UUID mapped = UUID.randomUUID();
		UUID expected = UUID.randomUUID();
		doReturn(mapped).when(mapper).apply(value1, value2);
		doReturn(expected).when(function).apply(mapped);

		assertThat(function.compose(mapper).apply(value1, value2)).isSameAs(expected);

		verify(function).apply(mapped);
	}

	@Test
	public void empty() {
		assertThat(Function.empty().apply(UUID.randomUUID())).isNull();
	}

	@Test
	public void of() {
		UUID parameter = UUID.randomUUID();
		UUID expected = UUID.randomUUID();

		assertThat(Function.of(expected).apply(parameter)).isSameAs(expected);
		assertThat(Function.of(null).apply(parameter)).isNull();

		Consumer<UUID> consumer = mock(Consumer.class);
		Supplier<UUID> supplier = mock(Supplier.class);

		doReturn(expected).when(supplier).get();

		assertThat(Function.of(consumer, supplier).apply(parameter)).isSameAs(expected);

		verify(consumer, only()).accept(parameter);
		verify(supplier, only()).get();
	}

	@Test
	public void withDefault() {
		UUID result = UUID.randomUUID();
		UUID value = UUID.randomUUID();
		UUID defaultValue = UUID.randomUUID();
		UUID defaultResult = UUID.randomUUID();
		UnaryOperator<UUID> function = mock(UnaryOperator.class);
		doCallRealMethod().when(function).withDefault(any(), any());
		Predicate<UUID> predicate = mock(Predicate.class);
		Supplier<UUID> supplier = mock(Supplier.class);

		doReturn(false, true).when(predicate).test(value);
		doReturn(defaultValue).when(supplier).get();
		doReturn(result).when(function).apply(value);
		doReturn(defaultResult).when(function).apply(defaultValue);

		assertThat(function.withDefault(predicate, supplier).apply(value)).isSameAs(defaultResult);
		assertThat(function.withDefault(predicate, supplier).apply(value)).isSameAs(result);
	}

	@Test
	public void orDefault() {
		UUID result = UUID.randomUUID();
		UUID value = UUID.randomUUID();
		UUID defaultResult = UUID.randomUUID();
		UnaryOperator<UUID> function = mock(UnaryOperator.class);
		doCallRealMethod().when(function).orDefault(any(), any());
		Predicate<UUID> predicate = mock(Predicate.class);
		Supplier<UUID> supplier = mock(Supplier.class);

		doReturn(false, true).when(predicate).test(value);
		doReturn(defaultResult).when(supplier).get();
		doReturn(result).when(function).apply(value);

		assertThat(function.orDefault(predicate, supplier).apply(value)).isSameAs(defaultResult);
		assertThat(function.orDefault(predicate, supplier).apply(value)).isSameAs(result);
	}

	@Test
	public void entry() {
		UnaryOperator<UUID> keyFunction = mock(UnaryOperator.class);
		UnaryOperator<UUID> valueFunction = mock(UnaryOperator.class);

		UUID sourceKey = UUID.randomUUID();
		UUID resultKey = UUID.randomUUID();
		UUID sourceValue = UUID.randomUUID();
		UUID resultValue = UUID.randomUUID();

		doReturn(resultKey).when(keyFunction).apply(sourceKey);
		doReturn(resultValue).when(valueFunction).apply(sourceValue);

		UnaryOperator<Map.Entry<UUID, UUID>> function = UnaryOperator.entry(keyFunction, valueFunction);

		Map.Entry<UUID, UUID> result = function.apply(Map.entry(sourceKey, sourceValue));

		assertThat(result.getKey()).isSameAs(resultKey);
		assertThat(result.getValue()).isSameAs(resultValue);
	}
}
