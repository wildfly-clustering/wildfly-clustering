/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.util.List;

import org.jboss.marshalling.ObjectTable;
import org.jboss.marshalling.Unmarshaller;

/**
 * @author Paul Ferraro
 */
public class IdentityObjectTable implements ObjectTable {

	private final IdentityTable<Object> table;
	private final Writer writer;

	public IdentityObjectTable(List<Object> objects) {
		this.table = IdentityTable.from(List.copyOf(objects));
		this.writer = (marshaller, object) -> this.table.findWriter(object).accept(marshaller, object);
	}

	@Override
	public Writer getObjectWriter(Object object) throws IOException {
		return this.table.findWriter(object) != null ? this.writer : null;
	}

	@Override
	public Object readObject(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException {
		return this.table.read(unmarshaller);
	}
}
