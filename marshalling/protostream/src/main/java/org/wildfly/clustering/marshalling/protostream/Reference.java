/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.function.IntSupplier;

/**
 * Encapsulates an object reference.
 * @author Paul Ferraro
 */
public class Reference implements IntSupplier {
	private final int id;

	/**
	 * Constructs a new reference with the specified identifier.
	 * @param id a reference identifier
	 */
	public Reference(int id) {
		this.id = id;
	}

	@Override
	public int getAsInt() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object object) {
		return (object instanceof Reference reference) ? this.id == reference.id : false;
	}

	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
}
