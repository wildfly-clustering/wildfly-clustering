/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import org.wildfly.clustering.server.infinispan.CacheContainerGroupMember;
import org.wildfly.clustering.server.jgroups.dispatcher.CommandDispatcherITCase;

/**
 * @author Paul Ferraro
 */
public class CacheContainerCommandDispatcherITCase extends CommandDispatcherITCase<CacheContainerGroupMember> {

	public CacheContainerCommandDispatcherITCase() {
		super(CacheContainerCommandDispatcherITCaseConfiguration::new);
	}
}
