/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

import java.util.function.Supplier;

/**
 * A factory for creating identifiers.
 * @param <I> the identifier type
 * @author Paul Ferraro
 */
public interface IdentifierFactory<I> extends Supplier<I>, Restartable {

}
