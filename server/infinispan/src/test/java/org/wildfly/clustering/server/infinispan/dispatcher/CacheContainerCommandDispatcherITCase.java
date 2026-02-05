/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.jgroups.dispatcher.AbstractCommandDispatcherITCase;

/**
 * @author Paul Ferraro
 */
public class CacheContainerCommandDispatcherITCase extends AbstractCommandDispatcherITCase<CacheContainerGroupMember, CacheContainerCommandDispatcherFactory> {

	public CacheContainerCommandDispatcherITCase() {
		super(CacheContainerCommandDispatcherFactoryContext::new);
	}
}
