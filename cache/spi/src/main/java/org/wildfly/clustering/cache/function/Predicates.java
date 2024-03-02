/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.Predicate;

/**
 * @author Paul Ferraro
 */
public class Predicates {

	private Predicates() {
		// Hide
	}

	private static final Predicate<?> ALWAYS = value -> true;
	private static final Predicate<?> NEVER = value -> false;

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> always() {
		return (Predicate<T>) ALWAYS;
	}

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> never() {
		return (Predicate<T>) NEVER;
	}
}
