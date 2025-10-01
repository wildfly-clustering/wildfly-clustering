/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jboss.marshalling.ObjectTable;

/**
 * Provides object tables for serializable JDK singleton objects.
 * @author Paul Ferraro
 */
public enum DefaultObjectTableProvider implements Supplier<ObjectTable> {
	/** Singleton objects of {@link java.util}. */
	UTIL(List.of(
			Collections.emptyList(),
			Collections.emptyMap(),
			Collections.emptyNavigableMap(),
			Collections.emptyNavigableSet(),
			Collections.emptySet(),
			Collections.emptySortedMap(),
			Collections.emptySortedSet(),
			List.of(),
			Map.of(),
			Set.of())),
	;
	private final ObjectTable table;

	DefaultObjectTableProvider(List<Object> objects) {
		this.table = new IdentityObjectTable(objects);
	}

	@Override
	public ObjectTable get() {
		return this.table;
	}
}
