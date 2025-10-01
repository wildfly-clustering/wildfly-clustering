/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import java.util.Collection;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.function.SetRemoveFunction;

/**
 * A function used to perform remove/removeAll operations on a set of addresses.
 * @author Paul Ferraro
 */
public class AddressSetRemoveFunction extends SetRemoveFunction<Address> {
	/**
	 * Creates a function that removes the specified address from a set.
	 * @param address a cache container address
	 */
	public AddressSetRemoveFunction(Address address) {
		super(address);
	}

	/**
	 * Creates a function that removes the specified addresses from a set.
	 * @param addresses a cache container address
	 */
	public AddressSetRemoveFunction(Collection<Address> addresses) {
		super(addresses);
	}
}
