/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.wildfly.common.Assert.checkNotNullParam;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * {@link TwoWayKey2StringMapper} implementation that maps multiple {@link Formatter} instances.
 * Key is mapped to an padded hexadecimal index + the formatted key.
 * @author Paul Ferraro
 */
public class FormatterKeyMapper implements TwoWayKey2StringMapper {
	private static final int HEX_RADIX = 16;

	public static TwoWayKey2StringMapper load(ClassLoader loader) {
		List<Formatter<?>> formatters = new LinkedList<>();
		for (Formatter<?> formatter : ServiceLoader.load(Formatter.class, loader)) {
			formatters.add(formatter);
		}

		List<Formatter<?>> result = new ArrayList<>(formatters.size() + 6);
		// Add key formats for common key types
		result.add(Formatter.IDENTITY);
		result.add(Formatter.IDENTITY.wrap(Byte.class, Byte::valueOf));
		result.add(Formatter.IDENTITY.wrap(Short.class, Short::valueOf));
		result.add(Formatter.IDENTITY.wrap(Integer.class, Integer::valueOf));
		result.add(Formatter.IDENTITY.wrap(Long.class, Long::valueOf));
		result.add(Formatter.IDENTITY.wrap(UUID.class, UUID::fromString));
		result.addAll(formatters);
		return new FormatterKeyMapper(result);
	}

	private final Map<Class<?>, Integer> indexes = new IdentityHashMap<>();
	private final List<Formatter<Object>> formatters;
	private final int padding;

	@SuppressWarnings("unchecked")
	public FormatterKeyMapper(List<? extends Formatter<?>> formatters) {
		this.formatters = (List<Formatter<Object>>) (List<?>) formatters;
		for (int i = 0; i < this.formatters.size(); ++i) {
			this.indexes.put(this.formatters.get(i).getType(), i);
		}
		// Determine number of characters to reserve for index
		this.padding = (int) (Math.log((double) this.formatters.size() - 1) / Math.log(HEX_RADIX)) + 1;
	}

	@Override
	public boolean isSupportedType(Class<?> keyType) {
		return this.indexes.containsKey(keyType);
	}

	@Override
	public String getStringMapping(Object key) {
		checkNotNullParam("key", key);
		Integer index = this.indexes.get(key.getClass());
		if (index == null) {
			throw new IllegalArgumentException(key.getClass().getName());
		}
		Formatter<Object> formatter = this.formatters.get(index);
		return String.format("%0" + this.padding + "X%s", index, formatter.format(key));
	}

	@Override
	public Object getKeyMapping(String value) {
		int index = Integer.parseUnsignedInt(value.substring(0, this.padding), HEX_RADIX);
		Formatter<Object> formatter = this.formatters.get(index);
		return formatter.parse(value.substring(this.padding));
	}
}
