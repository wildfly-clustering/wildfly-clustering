/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.jboss;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jboss.marshalling.ClassTable;
import org.jboss.marshalling.ObjectTable;

/**
 * {@link org.jboss.marshalling.ClassTable} implementation that dynamically an optional {@link ClassTable} instance per {@link ClassLoader}.
 * @author Paul Ferraro
 */
public class LoadedObjectTable extends org.jboss.marshalling.ChainingObjectTable {

	public LoadedObjectTable(ClassLoader loader) {
		this(List.of(loader));
	}

	public LoadedObjectTable(List<ClassLoader> loaders) {
		super(load(loaders));
	}

	private static ObjectTable[] load(List<ClassLoader> loaders) {
		List<ObjectTable> loadedTables = new ArrayList<>(loaders.size());
		for (ClassLoader loader : loaders) {
			ServiceLoader.load(ObjectTable.class, loader).findFirst().ifPresent(loadedTables::add);
		}
		Stream<ObjectTable> tables = EnumSet.allOf(DefaultObjectTableProvider.class).stream().map(Supplier::get);
		if (!loadedTables.isEmpty()) {
			tables = Stream.concat(tables, loadedTables.stream());
		}
		return tables.toArray(ObjectTable[]::new);
	}
}
