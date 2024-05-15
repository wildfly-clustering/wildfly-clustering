/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import org.wildfly.clustering.server.infinispan.CacheContainerGroup;
import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.provider.ServiceProviderRegistry;

/**
 * @author Paul Ferraro
 * @param <T> the service type
 */
public interface CacheContainerServiceProviderRegistry<T> extends ServiceProviderRegistry<T, CacheContainerGroupMember> {

	@Override
	CacheContainerGroup getGroup();
}
