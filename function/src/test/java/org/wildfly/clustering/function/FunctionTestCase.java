/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
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
		doCallRealMethod().when(function).compose(ArgumentMatchers.<BiFunction<UUID, UUID, UUID>>any());
		BiFunction<UUID, UUID, UUID> mapper = mock(BiFunction.class);
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
		Function<UUID, UUID> function = mock(Function.class);
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
		Function<UUID, UUID> function = mock(Function.class);
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
	public void handle() {
		Function<UUID, UUID> function = mock(Function.class);
		BiFunction<UUID, RuntimeException, UUID> handler = mock(BiFunction.class);
		doCallRealMethod().when(function).handle(any());

		UUID goodValue = UUID.randomUUID();
		UUID badValue = UUID.randomUUID();
		UUID result = UUID.randomUUID();
		UUID handled = UUID.randomUUID();
		RuntimeException exception = new RuntimeException();

		doReturn(result).when(function).apply(goodValue);
		doThrow(exception).when(function).apply(badValue);
		doReturn(handled).when(handler).apply(badValue, exception);

		assertThat(function.handle(handler).apply(goodValue)).isSameAs(result);
		assertThat(function.handle(handler).apply(badValue)).isSameAs(handled);

		verify(function).apply(goodValue);
		verify(function).apply(badValue);
		verify(handler).apply(badValue, exception);
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

		Function<Map.Entry<UUID, UUID>, Map.Entry<UUID, UUID>> function = Function.entry(keyFunction, valueFunction);

		Map.Entry<UUID, UUID> result = function.apply(Map.entry(sourceKey, sourceValue));

		assertThat(result.getKey()).isSameAs(resultKey);
		assertThat(result.getValue()).isSameAs(resultValue);
	}

	@Test
	public void optional() {
		Function<UUID, UUID> function = mock(Function.class);
		UUID parameter = UUID.randomUUID();
		UUID expected = UUID.randomUUID();

		doCallRealMethod().when(function).optional();
		doReturn(expected).when(function).apply(parameter);

		Function<Optional<UUID>, Optional<UUID>> optional = function.optional();

		assertThat(optional.apply(Optional.empty())).isEmpty();
		verify(function).optional();
		verifyNoMoreInteractions(function);

		assertThat(optional.apply(Optional.of(parameter))).get().isEqualTo(expected);

		verify(function).apply(parameter);
		verifyNoMoreInteractions(function);
	}
}
