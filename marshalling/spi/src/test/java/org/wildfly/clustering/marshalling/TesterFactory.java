/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

/**
 * @author Paul Ferraro
 */
public interface TesterFactory {

	<T> Tester<T> createTester(BiConsumer<T, T> assertion);

	default <T, U> Tester<T> createTester(Function<T, U> mapper) {
		return this.createTester(equalsAssertion(mapper));
	}

	default <T> Tester<T> createTester() {
		return this.createTester(Assertions::assertEquals);
	}

	default <T> Tester<T> createIdentityTester() {
		return this.createTester(Assertions::assertSame);
	}

	default <T> Tester<T> createKeyTester() {
		BiConsumer<T, T> hashCode = equalsAssertion(Object::hashCode);
		return this.createTester(hashCode.andThen(Assertions::assertEquals));
	}

	default <E extends Enum<E>> Runnable createTester(Class<E> enumClass) {
		Consumer<E> tester = this.createIdentityTester();
		return () -> EnumSet.allOf(enumClass).forEach(tester::accept);
	}

	default <E, T extends Collection<E>> Tester<T> createCollectionTester() {
		return this.createTester(TesterFactory::assertEquals);
	}

	default <E, T extends Collection<E>> Tester<T> createOrderedCollectionTester() {
		return this.createTester(TesterFactory::assertOrderedEquals);
	}

	default <K, V, T extends Map<K, V>> Tester<T> createMapTester() {
		return this.createTester(TesterFactory::assertEquals);
	}

	default <K, V, T extends Map<K, V>> Tester<T> createOrderedMapTester() {
		return this.createTester(TesterFactory::assertOrderedEquals);
	}

	static <T, R> BiConsumer<T, T> equalsAssertion(Function<T, R> mapper) {
		return (expected, actual) -> Assertions.assertEquals(mapper.apply(expected), mapper.apply(actual));
	}

	static <E> void assertEquals(Collection<E> expected, Collection<E> actual) {
		assertEquals(expected, actual, actual::toString);
	}

	static <E> void assertEquals(Collection<E> expected, Collection<E> actual, Supplier<String> message) {
		Assertions.assertEquals(expected.size(), actual.size(), message);
		Assertions.assertTrue(actual.containsAll(expected), message);
	}

	static <K, V> void assertEquals(Map<K, V> expected, Map<K, V> actual) {
		assertEquals(expected, actual, actual::toString);
	}

	static <K, V> void assertEquals(Map<K, V> expected, Map<K, V> actual, Supplier<String> message) {
		assertEquals(expected.keySet(), actual.keySet(), message);
		for (Map.Entry<K, V> entry : expected.entrySet()) {
			Assertions.assertEquals(entry.getValue(), actual.get(entry.getKey()), message);
		}
	}

	static <E> void assertOrderedEquals(Collection<E> expected, Collection<E> actual) {
		assertOrderedEquals(expected, actual, actual::toString);
	}

	static <E> void assertOrderedEquals(Collection<E> expected, Collection<E> actual, Supplier<String> message) {
		assertEquals(expected, actual, message);
		Iterator<E> expectedIterator = expected.iterator();
		Iterator<E> actualIterator = actual.iterator();
		while (expectedIterator.hasNext()) {
			Assertions.assertEquals(expectedIterator.next(), actualIterator.next());
		}
	}

	static <K, V> void assertOrderedEquals(Map<K, V> expected, Map<K, V> actual) {
		assertOrderedEquals(expected, actual, actual::toString);
	}

	static <K, V> void assertOrderedEquals(Map<K, V> expected, Map<K, V> actual, Supplier<String> message) {
		assertOrderedEquals(expected.keySet(), actual.keySet(), message);
		assertEquals(expected, actual, message);
	}
}
