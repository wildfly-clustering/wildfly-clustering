/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.util.List;

import org.jboss.marshalling.ClassTable;
import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.Unmarshaller;
import org.wildfly.common.function.ExceptionBiConsumer;

/**
 * A {@link ClassTable} based on an {@link IdentityTable}.
 * @author Paul Ferraro
 */
public class IdentityClassTable implements ClassTable {

	private final IdentityTable<Class<?>> table;

	public IdentityClassTable(List<Class<?>> classes) {
		this.table = IdentityTable.from(classes);
	}

	@Override
	public Writer getClassWriter(Class<?> targetClass) throws IOException {
		ExceptionBiConsumer<Marshaller, Class<?>, IOException> writer = this.table.findWriter(targetClass);
		return writer != null ? writer::accept : null;
	}

	@Override
	public Class<?> readClass(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException {
		return this.table.read(unmarshaller);
	}
}
