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
 * Encapsulates the configuration of a {@link EmbeddedCacheManagerGroup}.
 * @param <A> the group member address type
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface EmbeddedCacheManagerGroupConfiguration<A extends Comparable<A>, M extends GroupMember<A>> extends EmbeddedCacheContainerConfiguration {
	/**
	 * Returns the decorated group.
	 * @return the decorated group.
	 */
	Group<A, M> getGroup();

	/**
	 * Returns a wrapper function that converts the group member identifier of the decorated group to an Infinispan address.
	 * @return a wrapper function that converts the group member identifier of the decorated group to an Infinispan address.
	 */
	Function<A, Address> getAddressWrapper();
}
