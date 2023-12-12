/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.dispatcher;

import org.wildfly.clustering.server.dispatcher.CommandDispatcher;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.local.LocalGroup;
import org.wildfly.clustering.server.local.LocalGroupMember;

/**
 * @author Paul Ferraro
 */
public interface LocalCommandDispatcherFactory extends GroupCommandDispatcherFactory<String, LocalGroupMember> {

	@Override
	LocalGroup getGroup();

	static LocalCommandDispatcherFactory of(LocalGroup group) {
		return new LocalCommandDispatcherFactory() {
			@Override
			public LocalGroup getGroup() {
				return group;
			}

			@Override
			public <C> CommandDispatcher<LocalGroupMember, C> createCommandDispatcher(Object id, C context, ClassLoader loader) {
				return this.createCommandDispatcher(id, context);
			}

			@Override
			public <C> CommandDispatcher<LocalGroupMember, C> createCommandDispatcher(Object id, C context) {
				return new LocalCommandDispatcher<>(group.getLocalMember(), context);
			}
		};
	}
}
