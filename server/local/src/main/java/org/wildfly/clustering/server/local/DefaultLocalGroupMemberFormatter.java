/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;

/**
 * Resolver for a {@link DefaultLocalGroupMember}.
 * @author Paul Ferraro
 */
@MetaInfServices(Formatter.class)
public class DefaultLocalGroupMemberFormatter extends Formatter.Provided<DefaultLocalGroupMember> {

	public DefaultLocalGroupMemberFormatter() {
		super(Formatter.IDENTITY.wrap(DefaultLocalGroupMember.class, DefaultLocalGroupMember::new, DefaultLocalGroupMember::getName));
	}
}
