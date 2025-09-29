/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.dispatcher;

import java.util.function.Function;

import org.infinispan.remoting.transport.Address;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.group.GroupMember;
import org.wildfly.clustering.server.infinispan.EmbeddedCacheManagerGroupConfiguration;

/**
 * Encapsulates configuration of an {@link EmbeddedCacheManagerCommandDispatcherFactory}.
 * @param <A> the address type for group members
 * @param <M> the group member type
 * @author Paul Ferraro
 */
public interface EmbeddedCacheManagerCommandDispatcherFactoryConfiguration<A extends Comparable<A>, M extends GroupMember<A>> extends EmbeddedCacheManagerGroupConfiguration<A, M> {
	/**
	 * Returns the decorated command dispatcher factory
	 * @return the decorated command dispatcher factory
	 */
	GroupCommandDispatcherFactory<A, M> getCommandDispatcherFactory();

	/**
	 * Returns a function that converts an Infinispan address to the identifier expected by the decorated command dispatcher factory.
	 * @return a function that converts an Infinispan address to the identifier expected by the decorated command dispatcher factory.
	 */
	Function<Address, A> getAddressUnwrapper();
}
