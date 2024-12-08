/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Object2DArrayAssert;
import org.assertj.core.api.ObjectArrayAssert;
import org.assertj.core.api.ObjectAssert;

/**
 * @author Paul Ferraro
 */
public interface TesterFactory {

	default <A extends Assert<A, T>, T> Tester<T> createTester(Function<T, A> assertFactory, BiConsumer<A, T> assertion) {
		return this.createTester(new BiConsumer<>() {
			@Override
			public void accept(T expected, T actual) {
				assertion.accept(assertFactory.apply(actual), expected);
			}
		});
	}

	<T> Tester<T> createTester(BiConsumer<T, T> assertion);

	default <T> Tester<T> createTester() {
		return this.createTester(Assertions::assertThat, Assert::isEqualTo);
	}

	default <T> Tester<T> createIdentityTester() {
		return this.createTester(Assertions::assertThat, Assert::isSameAs);
	}

	default <T> Tester<T> createKeyTester() {
		BiConsumer<ObjectAssert<T>, T> assertHashCode = Assert::hasSameHashCodeAs;
		return this.createTester(Assertions::assertThat, assertHashCode.andThen(Assert::isEqualTo));
	}

	default <E extends Enum<E>> Runnable createTester(Class<E> enumClass) {
		Consumer<E> tester = this.createIdentityTester();
		return () -> EnumSet.allOf(enumClass).forEach(tester::accept);
	}

	default <E> Tester<E[]> createArrayTester() {
		Function<E[], ObjectArrayAssert<E>> arrayFactory = ObjectArrayAssert::new;
		return this.createTester(arrayFactory, ObjectArrayAssert::containsExactly);
	}

	default <E> Tester<E[][]> create2DArrayTester() {
		Function<E[][], Object2DArrayAssert<E>> arrayFactory = Object2DArrayAssert::new;
		return this.createTester(arrayFactory, Object2DArrayAssert::isDeepEqualTo);
	}

	default <E, T extends Collection<E>> Tester<T> createCollectionTester() {
		return this.createTester(new BiConsumer<>() {
			@Override
			public void accept(T expected, T actual) {
				Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
			}
		});
	}

	default <E, T extends Collection<E>> Tester<T> createOrderedCollectionTester() {
		return this.createTester(new BiConsumer<>() {
			@Override
			public void accept(T expected, T actual) {
				Assertions.assertThat(actual).containsExactlyElementsOf(expected);
			}
		});
	}

	default <K, V, T extends Map<K, V>> Tester<T> createMapTester() {
		return this.createTester(new BiConsumer<>() {
			@Override
			public void accept(T expected, T actual) {
				Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
			}
		});
	}

	default <K, V, T extends Map<K, V>> Tester<T> createOrderedMapTester() {
		return this.createTester(new BiConsumer<>() {
			@Override
			public void accept(T expected, T actual) {
				Assertions.assertThat(actual).containsExactlyEntriesOf(expected);
			}
		});
	}
}
