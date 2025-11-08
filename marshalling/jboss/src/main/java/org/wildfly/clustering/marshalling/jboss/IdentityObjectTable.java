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
 * An {@link ObjectTable} based on an {@link IdentityTable}.
 * @author Paul Ferraro
 */
public class IdentityObjectTable implements ObjectTable {

	private final IdentityTable<Object> table;

	/**
	 * Creates an object table for the specified objects.
	 * @param objects a list of objects.
	 */
	public IdentityObjectTable(List<Object> objects) {
		this.table = IdentityTable.from(objects);
	}

	@Override
	public Writer getObjectWriter(Object object) {
		Writable<Object> writer = this.table.findWriter(object);
		return writer != null ? writer::write : null;
	}

	@Override
	public Object readObject(Unmarshaller unmarshaller) throws IOException {
		return this.table.read(unmarshaller);
	}
}
