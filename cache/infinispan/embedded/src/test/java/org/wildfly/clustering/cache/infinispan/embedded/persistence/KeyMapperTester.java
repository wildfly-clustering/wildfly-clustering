/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.BiConsumer;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.junit.jupiter.api.Assertions;
import org.wildfly.clustering.marshalling.Tester;

/**
 * Tester for a {@link TwoWayKey2StringMapper}.
 * @param <K> the test key type
 * @author Paul Ferraro
 */
public class KeyMapperTester<K> implements Tester<K> {

	private final TwoWayKey2StringMapper mapper;

	public KeyMapperTester(TwoWayKey2StringMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public void test(K key) {
		this.test(key, Assertions::assertEquals);
	}

	@Override
	public void test(K key, BiConsumer<K, K> assertion) {
		assertTrue(this.mapper.isSupportedType(key.getClass()));

		String mapping = this.mapper.getStringMapping(key);

		@SuppressWarnings("unchecked")
		K result = (K) this.mapper.getKeyMapping(mapping);

		assertion.accept(key, result);
	}
}
