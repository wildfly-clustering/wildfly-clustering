/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.scheduler;

/**
 * Scheduler service that does not require predetermined entry meta data.
 * @author Paul Ferraro
 * @param <K> the scheduled entry identifier type
 * @param <V> the scheduled entry metadata type
 */
public interface SchedulerService<K, V> extends Scheduler<K, V>, org.wildfly.clustering.server.scheduler.SchedulerService<K, V> {

}
