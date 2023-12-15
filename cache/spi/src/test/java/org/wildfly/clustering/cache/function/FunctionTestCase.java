/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;


import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class FunctionTestCase {
	@Test
	public void set() {
		Set<String> result = new SetAddFunction<>("foo").apply(null, null);
		assertNotNull(result);
		assertTrue(result.contains("foo"));

		Set<String> result2 = new SetAddFunction<>("bar").apply(null, result);
		assertNotNull(result2);
		assertNotSame(result, result2);
		assertTrue(result2.contains("foo"));
		assertTrue(result2.contains("bar"));

		Set<String> result3 = new SetAddFunction<>(Set.of("baz", "qux")).apply(null, result2);
		assertNotNull(result3);
		assertNotSame(result2, result3);
		assertTrue(result3.contains("foo"));
		assertTrue(result3.contains("bar"));
		assertTrue(result3.contains("baz"));
		assertTrue(result3.contains("qux"));

		Set<String> result4 = new SetRemoveFunction<>("foo").apply(null, result3);
		assertNotNull(result4);
		assertNotSame(result3, result4);
		assertFalse(result4.contains("foo"));
		assertTrue(result4.contains("bar"));
		assertTrue(result4.contains("baz"));
		assertTrue(result4.contains("qux"));

		Set<String> result5 = new SetRemoveFunction<>(Set.of("bar", "baz")).apply(null, result4);
		assertNotNull(result5);
		assertNotSame(result4, result5);
		assertFalse(result5.contains("foo"));
		assertFalse(result5.contains("bar"));
		assertFalse(result5.contains("baz"));
		assertTrue(result5.contains("qux"));

		Set<String> result6 = new SetRemoveFunction<>("qux").apply(null, result5);
		assertNull(result6);
	}

	@Test
	public void map() {
		Map<String, String> result = new MapPutFunction<>("foo", "a").apply(null, null);
		assertNotNull(result);
		assertTrue(result.containsKey("foo"));

		Map<String, String> result2 = new MapPutFunction<>("bar", "b").apply(null, result);
		assertNotNull(result2);
		assertNotSame(result, result2);
		assertTrue(result2.containsKey("foo"));
		assertTrue(result2.containsKey("bar"));

		Map<String, String> result3 = new MapRemoveFunction<String, String>("foo").apply(null, result2);
		assertNotNull(result3);
		assertNotSame(result2, result3);
		assertFalse(result3.containsKey("foo"));
		assertTrue(result3.containsKey("bar"));

		Map<String, String> result4 = new MapRemoveFunction<String, String>("bar").apply(null, result3);
		assertNull(result4);

		Map<String, String> result5 = new MapComputeFunction<>(Map.of("foo", "a", "bar", "b")).apply(null, result4);
		assertNotNull(result5);
		assertEquals(2, result5.size());
		assertEquals("a", result5.get("foo"));
		assertEquals("b", result5.get("bar"));

		Map<String, String> updates = new TreeMap<>();
		updates.put("foo", null);
		updates.put("bar", "c");
		Map<String, String> result6 = new MapComputeFunction<>(updates).apply(null, result5);
		assertNotNull(result6);
		assertEquals(1, result6.size());
		assertFalse(result6.containsKey("foo"));
		assertEquals("c", result6.get("bar"));

		Map<String, String> result7 = new MapComputeFunction<>(Collections.<String, String>singletonMap("bar", null)).apply(null, result6);
		assertNull(result7);
	}

	@Test
	public void marshalling() throws IOException {
		MarshallingTesterFactory factory = new ProtoStreamTesterFactory();
		factory.createTester().test(new SetAddFunction<>(List.of("foo", "bar")));
		factory.createTester().test(new SetRemoveFunction<>(List.of("foo", "bar")));
		factory.createTester().test(new MapPutFunction<>("foo", "bar"));
		factory.createTester().test(new MapRemoveFunction<>("foo"));
		Map<String, String> values = new TreeMap<>();
		values.put("foo", "bar");
		values.put("baz", null);
		factory.createTester().test(new MapComputeFunction<>(values));
	}
}
