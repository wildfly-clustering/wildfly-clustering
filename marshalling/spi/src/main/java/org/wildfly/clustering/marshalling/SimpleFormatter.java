/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.function.Function;

/**
 * {@link Formatter} for keys with a simple string representation.
 * @param <T> the target type of this formatter
 * @author Paul Ferraro
 */
public class SimpleFormatter<T> implements Formatter<T> {

	private final Class<T> targetClass;
	private final Function<String, T> parser;
	private final Function<T, String> formatter;

	public SimpleFormatter(Class<T> targetClass, Function<String, T> parser) {
		this(targetClass, parser, Object::toString);
	}

	public SimpleFormatter(Class<T> targetClass, Function<String, T> parser, Function<T, String> formatter) {
		this.targetClass = targetClass;
		this.parser = parser;
		this.formatter = formatter;
	}

	@Override
	public Class<T> getTargetClass() {
		return this.targetClass;
	}

	@Override
	public T parse(String value) {
		return this.parser.apply(value);
	}

	@Override
	public String format(T key) {
		return this.formatter.apply(key);
	}
}
