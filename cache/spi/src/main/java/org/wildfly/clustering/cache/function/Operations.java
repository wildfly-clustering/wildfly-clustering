/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Defines operations for creating and copying an operable object.
 * @author Paul Ferraro
 * @param <T> the operable object type
 */
public interface Operations<T> {

	/**
	 * Returns an operator used to copy the operable object (for copy-on-write operations).
	 * @return an operator used to copy the operable object (for copy-on-write operations).
	 */
	UnaryOperator<T> getCopier();

	/**
	 * Returns a factory for creating the operable object (for set-if-absent operations).
	 * @return a factory for creating the operable object (for set-if-absent operations).
	 */
	Supplier<T> getFactory();

	/**
	 * Returns the predicate used to determine if the operable object is empty.
	 * @return the predicate used to determine if the operable object is empty.
	 */
	Predicate<T> isEmpty();
}
