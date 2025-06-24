/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

/**
 * A nestable transactional suspended batch.
 * @author Paul Ferraro
 */
public interface TransactionalSuspendedBatch extends SuspendedParentBatch, Transactional {
}
