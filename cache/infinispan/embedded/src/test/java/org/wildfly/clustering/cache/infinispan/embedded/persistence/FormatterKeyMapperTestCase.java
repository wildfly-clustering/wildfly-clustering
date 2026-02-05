/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.IntStream;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * @author Paul Ferraro
 */
public class FormatterKeyMapperTestCase {

	enum Type {
		TYPE00 {},
		TYPE01 {},
		TYPE02 {},
		TYPE03 {},
		TYPE04 {},
		TYPE05 {},
		TYPE06 {},
		TYPE07 {},
		TYPE08 {},
		TYPE09 {},
		TYPE10 {},
		TYPE11 {},
		TYPE12 {},
		TYPE13 {},
		TYPE14 {},
		TYPE15 {},
		TYPE16 {},
		TYPE17 {},
	}

	@Test
	public void testSinglePadding() {
		TwoWayKey2StringMapper mapper = new FormatterKeyMapper(createPersistenceList(16));

		assertThat(mapper.isSupportedType(Type.TYPE00.getClass())).isTrue();
		assertThat(mapper.isSupportedType(Type.TYPE15.getClass())).isTrue();
		assertThat(mapper.isSupportedType(Type.TYPE16.getClass())).isFalse();
		assertThat(mapper.isSupportedType(Type.TYPE17.getClass())).isFalse();

		String result = mapper.getStringMapping(Type.TYPE00);
		assertThat(mapper.getKeyMapping(result)).isSameAs(Type.TYPE00);
		assertThat(result).isEqualTo("0TYPE00");

		result = mapper.getStringMapping(Type.TYPE15);
		assertThat(mapper.getKeyMapping(result)).isSameAs(Type.TYPE15);
		assertThat(result).isEqualTo("FTYPE15");
	}

	@Test
	public void testDoublePadding() {
		TwoWayKey2StringMapper mapper = new FormatterKeyMapper(createPersistenceList(17));

		assertThat(mapper.isSupportedType(Type.TYPE00.getClass())).isTrue();
		assertThat(mapper.isSupportedType(Type.TYPE15.getClass())).isTrue();
		assertThat(mapper.isSupportedType(Type.TYPE16.getClass())).isTrue();
		assertThat(mapper.isSupportedType(Type.TYPE17.getClass())).isFalse();

		String result = mapper.getStringMapping(Type.TYPE00);
		assertThat(mapper.getKeyMapping(result)).isSameAs(Type.TYPE00);
		assertThat(result).isEqualTo("00TYPE00");

		result = mapper.getStringMapping(Type.TYPE15);
		assertThat(mapper.getKeyMapping(result)).isSameAs(Type.TYPE15);
		assertThat(result).isEqualTo("0FTYPE15");

		result = mapper.getStringMapping(Type.TYPE16);
		assertThat(mapper.getKeyMapping(result)).isSameAs(Type.TYPE16);
		assertThat(result).isEqualTo("10TYPE16");
	}

	private static List<? extends Formatter<?>> createPersistenceList(int size) {
		return IntStream.range(0, size).mapToObj(index -> Formatter.Identity.INSTANCE.wrap(Type.values()[index].getClass(), Type::name, value -> Type.valueOf(value))).toList();
	}
}
