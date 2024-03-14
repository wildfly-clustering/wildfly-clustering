/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher.test;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.wildfly.clustering.server.dispatcher.Command;

/**
 * Test command.
 * @param <C> the command context type
 * @author Paul Ferraro
 */
public class IdentityCommand<C> implements Command<C, C, RuntimeException> {

	@ProtoFactory
	public IdentityCommand() {
	}

	@Override
	public C execute(C context) throws RuntimeException {
		return context;
	}
}
