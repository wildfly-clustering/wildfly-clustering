/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.assertj.core.api.Assertions.*;

import java.util.function.BiConsumer;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.TesterFactory;

/**
 * @author Paul Ferraro
 */
public interface FormatterTesterFactory extends TesterFactory {
	System.Logger LOGGER = System.getLogger(FormatterTesterFactory.class.getName());

	@Override
	default <T> Tester<T> createTester(BiConsumer<T, T> assertion) {
		TwoWayKey2StringMapper mapper = this.getMapper();
		return new Tester<>() {
			@Override
			public void accept(T key) {
				Class<?> keyClass = key.getClass();
				assertThat(mapper.isSupportedType(keyClass)).as(key::toString).isTrue();
				String string = mapper.getStringMapping(key);

				LOGGER.log(System.Logger.Level.DEBUG, "{0}\t{1}\t{2}\t{3}", mapper.getClass().getSimpleName(), keyClass.getCanonicalName(), key, string);

				@SuppressWarnings("unchecked")
				T remappedKey = (T) mapper.getKeyMapping(string);
				assertion.accept(key, remappedKey);
			}

			@Override
			public void reject(T key) {
				assertThat(mapper.isSupportedType(key.getClass())).as(key::toString).isFalse();
			}

			@Override
			public <E extends Throwable> void reject(T key, Class<E> expected) {
				assertThat(mapper.isSupportedType(key.getClass())).as(key::toString).isTrue();
				assertThatExceptionOfType(expected).as(key::toString).isThrownBy(() -> mapper.getStringMapping(key));
			}
		};
	}

	TwoWayKey2StringMapper getMapper();
}
