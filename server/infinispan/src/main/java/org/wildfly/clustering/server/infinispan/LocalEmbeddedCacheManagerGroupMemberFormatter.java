/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.Formatter;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.SimpleFormatter;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(Formatter.class)
public class LocalEmbeddedCacheManagerGroupMemberFormatter extends SimpleFormatter<LocalEmbeddedCacheManagerGroupMember> {

	public LocalEmbeddedCacheManagerGroupMemberFormatter() {
		super(LocalEmbeddedCacheManagerGroupMember.class, LocalEmbeddedCacheManagerGroupMember::new, LocalEmbeddedCacheManagerGroupMember::getName);
	}
}
