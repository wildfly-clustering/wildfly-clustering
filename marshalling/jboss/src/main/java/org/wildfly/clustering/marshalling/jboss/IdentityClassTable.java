/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.util.List;

import org.jboss.marshalling.ClassTable;
import org.jboss.marshalling.Unmarshaller;

/**
 * @author Paul Ferraro
 */
public class IdentityClassTable implements ClassTable {

	private final IdentityTable<Class<?>> table;
	private final Writer writer;

	public IdentityClassTable(List<Class<?>> classes) {
		this.table = IdentityTable.from(List.copyOf(classes));
		this.writer = (marshaller, targetClass) -> this.table.findWriter(targetClass).accept(marshaller, targetClass);
	}

	@Override
	public Writer getClassWriter(Class<?> targetClass) throws IOException {
		return this.table.findWriter(targetClass) != null ? this.writer : null;
	}

	@Override
	public Class<?> readClass(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException {
		return this.table.read(unmarshaller);
	}
}
