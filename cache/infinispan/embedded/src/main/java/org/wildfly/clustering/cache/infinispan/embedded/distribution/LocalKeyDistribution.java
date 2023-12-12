/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.distribution;

import java.util.Collections;
import java.util.List;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;

/**
 * Key distribution implementation for a local cache.
 * @author Paul Ferraro
 */
enum LocalKeyDistribution implements KeyDistribution {
	INSTANCE;

	@Override
	public Address getPrimaryOwner(Object key) {
		return LocalModeAddress.INSTANCE;
	}

	@Override
	public List<Address> getOwners(Object key) {
		return Collections.singletonList(LocalModeAddress.INSTANCE);
	}
}
