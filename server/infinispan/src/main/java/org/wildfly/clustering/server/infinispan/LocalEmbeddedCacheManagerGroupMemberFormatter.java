/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * Provides a formatter for a {@link LocalEmbeddedCacheManagerGroupMember}.
 * @author Paul Ferraro
 */
@MetaInfServices(Formatter.class)
public class LocalEmbeddedCacheManagerGroupMemberFormatter extends Formatter.Provided<LocalEmbeddedCacheManagerGroupMember> {
	/**
	 * Creates a group member formatter.
	 */
	public LocalEmbeddedCacheManagerGroupMemberFormatter() {
		super(Formatter.IDENTITY.wrap(LocalEmbeddedCacheManagerGroupMember.class, LocalEmbeddedCacheManagerGroupMember::getName, LocalEmbeddedCacheManagerGroupMember::new));
	}
}
