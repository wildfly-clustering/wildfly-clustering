/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.manager;

import org.wildfly.clustering.cache.batch.Batch;

/**
 * A manager of server-side state.
 * @author Paul Ferraro
 */
public interface Manager<I, B extends Batch> extends ManagerConfiguration<I, B>, Restartable {
}
