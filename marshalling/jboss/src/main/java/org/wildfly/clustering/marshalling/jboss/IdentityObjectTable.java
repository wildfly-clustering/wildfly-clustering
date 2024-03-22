/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.util.List;

import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.ObjectTable;
import org.jboss.marshalling.Unmarshaller;
import org.wildfly.common.function.ExceptionBiConsumer;

/**
 * An {@link ObjectTable} based on an {@link IdentityTable}.
 * @author Paul Ferraro
 */
public class IdentityObjectTable implements ObjectTable {

	private final IdentityTable<Object> table;

	public IdentityObjectTable(List<Object> objects) {
		this.table = IdentityTable.from(objects);
	}

	@Override
	public Writer getObjectWriter(Object object) throws IOException {
		ExceptionBiConsumer<Marshaller, Object, IOException> writer = this.table.findWriter(object);
		return writer != null ? writer::accept : null;
	}

	@Override
	public Object readObject(Unmarshaller unmarshaller) throws IOException, ClassNotFoundException {
		return this.table.read(unmarshaller);
	}
}
