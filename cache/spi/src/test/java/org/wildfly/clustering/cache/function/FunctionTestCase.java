/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * @author Paul Ferraro
 */
public class FunctionTestCase {
	@Test
	public void set() {
		Set<String> result = new SetAddFunction<>("foo").apply(null, null);
		assertThat(result).isNotNull().containsExactly("foo");

		Set<String> result2 = new SetAddFunction<>("bar").apply(null, result);
		assertThat(result2).isNotNull().isNotSameAs(result).containsExactlyInAnyOrder("foo", "bar");

		Set<String> result3 = new SetAddFunction<>(Set.of("baz", "qux")).apply(null, result2);
		assertThat(result3).isNotNull().isNotSameAs(result2).containsExactlyInAnyOrder("foo", "bar", "baz", "qux");

		Set<String> result4 = new SetRemoveFunction<>("foo").apply(null, result3);
		assertThat(result4).isNotNull().isNotSameAs(result3).containsExactlyInAnyOrder("bar", "baz", "qux");

		Set<String> result5 = new SetRemoveFunction<>(Set.of("bar", "baz")).apply(null, result4);
		assertThat(result5).isNotNull().isNotSameAs(result4).containsExactly("qux");

		Set<String> result6 = new SetRemoveFunction<>("qux").apply(null, result5);
		assertThat(result6).isNull();
	}

	@Test
	public void map() {
		Map<String, String> result = new MapPutFunction<>("foo", "a").apply(null, null);
		assertThat(result).isNotNull().containsExactlyEntriesOf(Map.of("foo", "a"));

		Map<String, String> result2 = new MapPutFunction<>("bar", "b").apply(null, result);
		assertThat(result2).isNotNull().isNotSameAs(result).containsExactlyInAnyOrderEntriesOf(Map.of("foo", "a", "bar", "b"));

		Map<String, String> result3 = new MapRemoveFunction<String, String>("foo").apply(null, result2);
		assertThat(result3).isNotNull().isNotSameAs(result2).containsExactlyEntriesOf(Map.of("bar", "b"));

		Map<String, String> result4 = new MapRemoveFunction<String, String>("bar").apply(null, result3);
		assertThat(result4).isNull();

		Map<String, String> result5 = new MapComputeFunction<>(Map.of("foo", "a", "bar", "b")).apply(null, result4);
		assertThat(result5).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of("foo", "a", "bar", "b"));

		Map<String, String> updates = new TreeMap<>();
		updates.put("foo", null);
		updates.put("bar", "c");
		Map<String, String> result6 = new MapComputeFunction<>(updates).apply(null, result5);
		assertThat(result6).isNotNull().containsExactlyEntriesOf(Map.of("bar", "c"));

		Map<String, String> result7 = new MapComputeFunction<>(Collections.<String, String>singletonMap("bar", null)).apply(null, result6);
		assertThat(result7).isNull();
	}

	@ParameterizedTest
	@TesterFactorySource
	public void marshalling(TesterFactory factory) {
		factory.createTester().accept(new SetAddFunction<>(List.of("foo", "bar")));
		factory.createTester().accept(new SetRemoveFunction<>(List.of("foo", "bar")));
		factory.createTester().accept(new MapPutFunction<>("foo", "bar"));
		factory.createTester().accept(new MapRemoveFunction<>("foo"));
		Map<String, String> values = new TreeMap<>();
		values.put("foo", "bar");
		values.put("baz", null);
		factory.createTester().accept(new MapComputeFunction<>(values));
	}
}
