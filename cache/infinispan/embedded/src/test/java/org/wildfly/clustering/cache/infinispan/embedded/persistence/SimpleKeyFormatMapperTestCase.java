/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * @author Paul Ferraro
 */
public class SimpleKeyFormatMapperTestCase {

	@Test
	public void test() {
		Formatter<Object> keyFormat = mock(Formatter.class);
		TwoWayKey2StringMapper mapper = new SimpleKeyFormatMapper(keyFormat);

		Object key = new Object();
		String formatted = "foo";

		when(keyFormat.getTargetClass()).thenReturn(Object.class);
		when(keyFormat.format(key)).thenReturn(formatted);
		when(keyFormat.parse(formatted)).thenReturn(key);

		assertSame(formatted, mapper.getStringMapping(key));
		assertSame(key, mapper.getKeyMapping(formatted));
		assertTrue(mapper.isSupportedType(Object.class));
		assertFalse(mapper.isSupportedType(Integer.class));
	}
}
