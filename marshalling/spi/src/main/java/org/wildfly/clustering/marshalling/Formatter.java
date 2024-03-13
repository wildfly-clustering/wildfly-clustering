/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

/**
 * Formats an objects into a string representation and back again.
 * @param <T> the formatted type
 * @author Paul Ferraro
 */
public interface Formatter<T> {
	/**
	 * The implementation class of the target key of this format.
	 * @return an implementation class
	 */
	Class<T> getTargetClass();

	/**
	 * Parses the key from the specified string.
	 * @param value a string representation of the key
	 * @return the parsed key
	 */
	T parse(String value);

	/**
	 * Formats the specified key to a string representation.
	 * @param key a key to format
	 * @return a string representation of the specified key.
	 */
	String format(T key);
}
