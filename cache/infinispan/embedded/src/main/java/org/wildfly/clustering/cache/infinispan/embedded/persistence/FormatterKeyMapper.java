/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import static org.wildfly.common.Assert.checkNotNullParam;

import java.security.PrivilegedAction;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
	private static final List<Formatter<?>> DEFAULT_FORMATTERS = List.of(
			Formatter.IDENTITY,
			Formatter.IDENTITY.wrap(Byte.class, Byte::valueOf),
			Formatter.IDENTITY.wrap(Short.class, Short::valueOf),
			Formatter.IDENTITY.wrap(Integer.class, Integer::valueOf),
			Formatter.IDENTITY.wrap(Long.class, Long::valueOf),
			Formatter.IDENTITY.wrap(UUID.class, UUID::fromString));

	public static TwoWayKey2StringMapper load(ClassLoader loader) {
		List<Formatter<?>> formatters = new LinkedList<>();
		formatters.addAll(DEFAULT_FORMATTERS);
		java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Void run() {
				ServiceLoader.load(Formatter.class, loader).forEach(formatters::add);
				return null;
			}
		});
		return new FormatterKeyMapper(formatters);
	}

	private final Map<Class<?>, Integer> indexes = new IdentityHashMap<>();
	private final List<Formatter<?>> formatters;
	private final int padding;

	public FormatterKeyMapper(List<? extends Formatter<?>> formatters) {
		this.formatters = List.copyOf(formatters);
		this.padding = this.padding();
	}

	private int padding() {
		for (int i = 0; i < this.formatters.size(); ++i) {
			this.indexes.put(this.formatters.get(i).getType(), i);
		}
		return (int) (Math.log((double) this.formatters.size() - 1) / Math.log(HEX_RADIX)) + 1;
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
		@SuppressWarnings("unchecked")
		Formatter<Object> formatter = (Formatter<Object>) this.formatters.get(index);
		return String.format(Locale.ROOT, "%0" + this.padding + "X%s", index, formatter.format(key));
	}

	@Override
	public Object getKeyMapping(String value) {
		int index = Integer.parseUnsignedInt(value.substring(0, this.padding), HEX_RADIX);
		@SuppressWarnings("unchecked")
		Formatter<Object> formatter = (Formatter<Object>) this.formatters.get(index);
		return formatter.parse(value.substring(this.padding));
	}
}
