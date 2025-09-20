/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.service.Service;

/**
 * A service that creating identifiers for a manager.
 * @param <I> the identifier type
 * @author Paul Ferraro
 */
public interface IdentifierFactoryService<I> extends Supplier<I>, Service {

}
