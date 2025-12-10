/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.session.SessionManagerFactory;

/**
 * @author Paul Ferraro
 * @param <P> parameters type
 * @param <C> session context type
 */
public interface SessionManagerFactoryContextProvider<P extends SessionManagerParameters, C> {

	<SC> Context<SessionManagerFactory<C, SC>> createContext(P parameters, String memberName, Supplier<SC> contextFactory);
}
