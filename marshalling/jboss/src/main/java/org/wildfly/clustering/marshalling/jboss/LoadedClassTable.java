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

/**
 * {@link org.jboss.marshalling.ClassTable} implementation that dynamically an optional {@link ClassTable} instance per {@link ClassLoader}.
 * @author Paul Ferraro
 */
public class LoadedClassTable extends org.jboss.marshalling.ChainingClassTable {

	public LoadedClassTable(ClassLoader loader) {
		this(List.of(loader));
	}

	public LoadedClassTable(List<ClassLoader> loaders) {
		super(load(loaders));
	}

	private static ClassTable[] load(List<ClassLoader> loaders) {
		List<ClassTable> loadedTables = new ArrayList<>(loaders.size());
		for (ClassLoader loader : loaders) {
			ServiceLoader.load(ClassTable.class, loader).findFirst().ifPresent(loadedTables::add);
		}
		Stream<ClassTable> tables = EnumSet.allOf(DefaultClassTableProvider.class).stream().map(Supplier::get);
		if (!loadedTables.isEmpty()) {
			tables = Stream.concat(tables, loadedTables.stream());
		}
		return tables.toArray(ClassTable[]::new);
	}
}
