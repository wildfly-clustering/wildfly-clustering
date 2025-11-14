/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.util.function.Function;

import org.jgroups.Address;
import org.jgroups.util.ExtendedUUID;
import org.jgroups.util.Util;

/**
 * Address generators for JGroups.
 * @author Paul Ferraro
 */
public enum AddressGenerator implements org.jgroups.stack.AddressGenerator {
	/** Generates {@link org.jgroups.util.UUID} instances */
	UUID(Util::createRandomAddress),
	/** Generates {@link ExtendedUUID} instances */
	EXTENDED_UUID(ExtendedUUID::randomUUID),
	;
	private final Function<String, Address> factory;

	AddressGenerator(Function<String, Address> factory) {
		this.factory = factory;
	}

	@Override
	@Deprecated
	public Address generateAddress() {
		return this.generateAddress(null);
	}

	@Override
	public Address generateAddress(String name) {
		return this.factory.apply(name);
	}
}
