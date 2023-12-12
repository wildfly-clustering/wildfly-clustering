/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups.dispatcher;

import org.wildfly.clustering.context.Contextualizer;
import org.wildfly.clustering.marshalling.MarshalledValueFactory;

/**
 * @author Paul Ferraro
 */
public interface CommandDispatcherContext<CC, MC> {
	CC getCommandContext();
	Contextualizer getContextualizer();
	MarshalledValueFactory<MC> getMarshalledValueFactory();
}
