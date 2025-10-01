/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import java.util.Collection;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.function.SetAddFunction;

/**
 * A function used to perform add/addAll operations on a set of addresses.
 * @author Paul Ferraro
 */
public class AddressSetAddFunction extends SetAddFunction<Address> {
	/**
	 * Creates a function that adds the specified address to a set.
	 * @param address a cache container address
	 */
	public AddressSetAddFunction(Address address) {
		super(address);
	}

	/**
	 * Creates a function that adds the specified addresses to a set.
	 * @param addresses a collection of cache container addresses
	 */
	public AddressSetAddFunction(Collection<Address> addresses) {
		super(addresses);
	}
}
