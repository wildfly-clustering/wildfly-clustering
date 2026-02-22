/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;

import org.wildfly.clustering.function.BiConsumer;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.function.Runner;
import org.wildfly.clustering.function.Supplier;

/**
 * Encapsulates thread-safe references to map entries.
 * @author Paul Ferraro
 * @param <K> the key type
 * @param <V> the value type
 */
public interface BlockingReferenceMap<K, V> extends Reference<Map<K, V>> {
	/**
	 * Returns a reference to the map entry for the specified key.
	 * @param key a map key
	 * @return a reference to the map entry for the specified key.
	 */
	BlockingReference<V> reference(K key);

	/**
	 * Returns a thread-safe map of the specified map.
	 * @param <K> the map key type
	 * @param <V> the map value type
	 * @param map a non-thread-safe map
	 * @return a thread-safe map of the specified map.
	 */
	static <K, V> BlockingReferenceMap<K, V> of(Map<K, V> map) {
		StampedLock lock = new StampedLock();
		Supplier<Map<K, V>> reader = Supplier.of(map).thenApply(Collections::unmodifiableMap);
		return new BlockingReferenceMap<>() {
			@Override
			public BlockingReference<V> reference(K key) {
				Supplier<V> reader = Supplier.of(key).thenApply(map::get);
				Consumer<V> writer = BiConsumer.of(map::put, Runner.of()).composeUnary(Function.of(key), Function.identity());
				BlockingReference.Reader<V> blockingReader = new BlockingReference.BlockingReferenceReader<>(lock, reader);
				BlockingReference.Writer<V> blockingWriter = new BlockingReference.BlockingReferenceWriter<>(lock, reader, writer);
				return new BlockingReference<>() {
					@Override
					public Reader<V> getReader() {
						return blockingReader;
					}

					@Override
					public Writer<V> getWriter() {
						return blockingWriter;
					}

					@Override
					public Writer<V> getWriter(Predicate<V> when) {
						return new ConditionalReferenceWriter<>(lock, reader, writer, when);
					}
				};
			}

			@Override
			public Reader<Map<K, V>> getReader() {
				return new BlockingReference.BlockingReferenceReader<>(lock, reader);
			}
		};
	}
}
