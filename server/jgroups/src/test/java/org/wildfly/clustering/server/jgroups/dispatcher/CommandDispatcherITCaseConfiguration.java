/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.dispatcher.CommandDispatcherFactory;

/**
 * @author Paul Ferraro
 */
public interface CommandDispatcherITCaseConfiguration<M extends GroupMember> extends AutoCloseable {

	CommandDispatcherFactory<M> getCommandDispatcherFactory();
}
