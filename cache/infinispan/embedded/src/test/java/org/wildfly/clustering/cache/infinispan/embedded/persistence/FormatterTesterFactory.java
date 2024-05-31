/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.BiConsumer;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.junit.jupiter.api.Assertions;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.TesterFactory;

/**
 * @author Paul Ferraro
 */
public interface FormatterTesterFactory extends TesterFactory {

	@Override
	default <T> Tester<T> createTester(BiConsumer<T, T> assertion) {
		TwoWayKey2StringMapper mapper = this.getMapper();
		return new Tester<>() {
			@Override
			public void accept(T key) {
				Class<?> keyClass = key.getClass();
				Assertions.assertTrue(mapper.isSupportedType(keyClass), key::toString);
				String string = mapper.getStringMapping(key);

				System.out.println(String.format("%s\t%s\t%s\t%s", mapper.getClass().getSimpleName(), keyClass.getCanonicalName(), key, string));

				@SuppressWarnings("unchecked")
				T remappedKey = (T) mapper.getKeyMapping(string);
				assertion.accept(key, remappedKey);
			}

			@Override
			public void reject(T key) {
				Assertions.assertFalse(mapper.isSupportedType(key.getClass()), key::toString);
			}

			@Override
			public <E extends Throwable> void reject(T key, Class<E> expected) {
				Assertions.assertTrue(mapper.isSupportedType(key.getClass()), key::toString);
				assertThrows(expected, () -> mapper.getStringMapping(key), key::toString);
			}
		};
	}

	TwoWayKey2StringMapper getMapper();
}
