/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.test.TestComparator;

/**
 * Generic tests for java.util.concurrent.* classes.
 * @author Paul Ferraro
 */
public abstract class AbstractConcurrentTestCase {
	private static final Map<Object, Object> BASIS = Stream.of(1, 2, 3, 4, 5).collect(Collectors.<Integer, Object, Object>toMap(i -> i, i -> Integer.toString(-i)));

	private final MarshallingTesterFactory factory;

	public AbstractConcurrentTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void testConcurrentHashMap() {
		Consumer<Map<Object, Object>> tester = this.factory.createMapTester();
		tester.accept(new ConcurrentHashMap<>(BASIS));
	}

	@Test
	public void testConcurrentHashSet() {
		ConcurrentHashMap.KeySetView<Object, Boolean> keySetView = ConcurrentHashMap.newKeySet();
		keySetView.addAll(BASIS.keySet());
		Consumer<ConcurrentHashMap.KeySetView<Object, Boolean>> tester = this.factory.createCollectionTester();
		tester.accept(keySetView);
	}

	@Test
	public void testConcurrentLinkedDeque() {
		Consumer<ConcurrentLinkedDeque<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(new ConcurrentLinkedDeque<>(BASIS.keySet()));
	}

	@Test
	public void testConcurrentLinkedQueue() {
		Consumer<ConcurrentLinkedQueue<Object>> tester = this.factory.createOrderedCollectionTester();
		tester.accept(new ConcurrentLinkedQueue<>(BASIS.keySet()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConcurrentSkipListMap() {
		Consumer<ConcurrentSkipListMap<Object, Object>> tester = this.factory.createMapTester();

		ConcurrentSkipListMap<Object, Object> map = new ConcurrentSkipListMap<>();
		map.putAll(BASIS);
		tester.accept(map);

		map = new ConcurrentSkipListMap<>((Comparator<Object>) (Comparator<?>) Comparator.reverseOrder());
		map.putAll(BASIS);
		tester.accept(map);

		map = new ConcurrentSkipListMap<>(new TestComparator<>());
		map.putAll(BASIS);
		tester.accept(map);

		tester.accept(new ConcurrentSkipListMap<>(BASIS));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConcurrentSkipListSet() {
		Consumer<ConcurrentSkipListSet<Object>> tester = this.factory.createCollectionTester();

		ConcurrentSkipListSet<Object> set = new ConcurrentSkipListSet<>();
		set.addAll(BASIS.keySet());
		tester.accept(set);

		set = new ConcurrentSkipListSet<>((Comparator<Object>) (Comparator<?>) Comparator.reverseOrder());
		set.addAll(BASIS.keySet());
		tester.accept(set);

		set = new ConcurrentSkipListSet<>(new TestComparator<>());
		set.addAll(BASIS.keySet());
		tester.accept(set);
	}

	@Test
	public void testCopyOnWriteArrayList() {
		Consumer<CopyOnWriteArrayList<Object>> tester = this.factory.createCollectionTester();
		tester.accept(new CopyOnWriteArrayList<>(BASIS.keySet()));
	}

	@Test
	public void testCopyOnWriteArraySet() {
		Consumer<CopyOnWriteArraySet<Object>> tester = this.factory.createCollectionTester();
		tester.accept(new CopyOnWriteArraySet<>(BASIS.keySet()));
	}

	@Test
	public void testTimeUnit() {
		this.factory.createTester(TimeUnit.class).run();
	}
}
