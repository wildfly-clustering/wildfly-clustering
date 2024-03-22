/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * @author Paul Ferraro
 */
public class KeyFormatterMapperTestCase {

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
		TwoWayKey2StringMapper mapper = new KeyFormatterMapper(createPersistenceList(16));

		assertTrue(mapper.isSupportedType(Type.TYPE00.getClass()));
		assertTrue(mapper.isSupportedType(Type.TYPE15.getClass()));
		assertFalse(mapper.isSupportedType(Type.TYPE16.getClass()));
		assertFalse(mapper.isSupportedType(Type.TYPE17.getClass()));

		String result = mapper.getStringMapping(Type.TYPE00);
		assertSame(Type.TYPE00, mapper.getKeyMapping(result));
		assertEquals("0TYPE00", result);

		result = mapper.getStringMapping(Type.TYPE15);
		assertSame(Type.TYPE15, mapper.getKeyMapping(result));
		assertEquals("FTYPE15", result);
	}

	@Test
	public void testDoublePadding() {
		TwoWayKey2StringMapper mapper = new KeyFormatterMapper(createPersistenceList(17));

		assertTrue(mapper.isSupportedType(Type.TYPE00.getClass()));
		assertTrue(mapper.isSupportedType(Type.TYPE15.getClass()));
		assertTrue(mapper.isSupportedType(Type.TYPE16.getClass()));
		assertFalse(mapper.isSupportedType(Type.TYPE17.getClass()));

		String result = mapper.getStringMapping(Type.TYPE00);
		assertSame(Type.TYPE00, mapper.getKeyMapping(result));
		assertEquals("00TYPE00", result);

		result = mapper.getStringMapping(Type.TYPE15);
		assertSame(Type.TYPE15, mapper.getKeyMapping(result));
		assertEquals("0FTYPE15", result);

		result = mapper.getStringMapping(Type.TYPE16);
		assertSame(Type.TYPE16, mapper.getKeyMapping(result));
		assertEquals("10TYPE16", result);
	}

	@SuppressWarnings("unchecked")
	private static List<? extends Formatter<?>> createPersistenceList(int size) {
		return IntStream.range(0, size).mapToObj(index -> Formatter.IDENTITY.wrap((Class<Type>) Type.values()[index].getClass(), Type::name, value -> Type.valueOf(value))).collect(Collectors.toList());
	}
}
