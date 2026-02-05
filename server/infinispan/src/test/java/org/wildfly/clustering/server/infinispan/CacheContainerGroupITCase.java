/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.jgroups.AbstractGroupITCase;

/**
 * Integration test for JChannel-based group implementation.
 * @author Paul Ferraro
 */
public class CacheContainerGroupITCase extends AbstractGroupITCase<Address, CacheContainerGroupMember, CacheContainerGroup> {

	public CacheContainerGroupITCase() {
		super(EmbeddedCacheManagerGroupContext::new, Address::toExtendedUUID);
	}
}
