/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * {@link Formatter} for complex types with multiple string fields.
 * @param <T> the formatted type
 * @author Paul Ferraro
 */
public class DelimitedFormatter<T> extends SimpleFormatter<T> {

	public DelimitedFormatter(Class<T> targetClass, String delimiter, Function<String[], T> parser, Function<T, String[]> formatter) {
		super(targetClass, value -> parser.apply(value.split(Pattern.quote(delimiter))), key -> String.join(delimiter, formatter.apply(key)));
	}
}
