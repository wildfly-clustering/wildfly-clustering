/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.infinispan.embedded;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheConfiguration;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @param <GM> the group member type
 * @author Paul Ferraro
 */
public interface InfinispanSessionManagerFactoryConfiguration<GM extends GroupMember<Address>> extends EmbeddedCacheConfiguration {

	GroupCommandDispatcherFactory<Address, GM> getCommandDispatcherFactory();
}
