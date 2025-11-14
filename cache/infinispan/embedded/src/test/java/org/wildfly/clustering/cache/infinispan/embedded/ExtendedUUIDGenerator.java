/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import org.infinispan.remoting.transport.NodeVersion;
import org.jgroups.Address;
import org.jgroups.stack.AddressGenerator;
import org.kohsuke.MetaInfServices;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(AddressGenerator.class)
public class ExtendedUUIDGenerator implements AddressGenerator {

	@Override
	public Address generateAddress() {
		return this.generateAddress(null);
	}

	@Override
	public Address generateAddress(String name) {
		return org.infinispan.remoting.transport.Address.randomUUID(name, NodeVersion.INSTANCE, null, null, null);
	}
}
