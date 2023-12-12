/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.infinispan;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.cache.infinispan.embedded.EmbeddedCacheContainerConfiguration;
import org.wildfly.clustering.server.group.Group;
import org.wildfly.clustering.server.group.GroupMember;

/**
 * @author Paul Ferraro
 */
public interface EmbeddedCacheManagerGroupConfiguration<A extends Comparable<A>, M extends GroupMember<A>> extends EmbeddedCacheContainerConfiguration {

	Group<A, M> getGroup();

	Function<A, Address> getAddressWrapper();
}
