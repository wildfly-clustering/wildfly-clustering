/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Unit test for {@link Function}.
 * @author Paul Ferraro
 */
public class FunctionTestCase {

	@Test
	public void compose() {
		Function<UUID, UUID> function = mock(Function.class);
		doCallRealMethod().when(function).compose(ArgumentMatchers.<Function<UUID, UUID>>any());
		Function<UUID, UUID> mapper = mock(Function.class);
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
		Function<UUID, UUID> function = mock(Function.class);
		doCallRealMethod().when(function).composeBinary(ArgumentMatchers.<BiFunction<UUID, UUID, UUID>>any());
		BiFunction<UUID, UUID, UUID> mapper = mock(BiFunction.class);
		UUID value1 = UUID.randomUUID();
		UUID value2 = UUID.randomUUID();
		UUID mapped = UUID.randomUUID();
		UUID expected = UUID.randomUUID();
		doReturn(mapped).when(mapper).apply(value1, value2);
		doReturn(expected).when(function).apply(mapped);

		assertThat(function.composeBinary(mapper).apply(value1, value2)).isSameAs(expected);

		verify(function).apply(mapped);
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
	public void when() {
		UUID accepted = UUID.randomUUID();
		UUID acceptedResult = UUID.randomUUID();
		UUID rejected = UUID.randomUUID();
		UUID rejectedResult = UUID.randomUUID();

		Predicate<UUID> predicate = mock(Predicate.class);

		doReturn(true).when(predicate).test(accepted);
		doReturn(false).when(predicate).test(rejected);

		Function<UUID, UUID> function = Function.when(predicate, Function.of(acceptedResult), Function.of(rejectedResult));

		assertThat(function.apply(accepted)).isSameAs(acceptedResult);
		assertThat(function.apply(rejected)).isSameAs(rejectedResult);
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

		Function<Map.Entry<UUID, UUID>, Map.Entry<UUID, UUID>> function = Function.entry(Function.of(Map.Entry::getKey, keyFunction), Function.of(Map.Entry::getValue, valueFunction));

		Map.Entry<UUID, UUID> result = function.apply(Map.entry(sourceKey, sourceValue));

		assertThat(result.getKey()).isSameAs(resultKey);
		assertThat(result.getValue()).isSameAs(resultValue);
	}
}
