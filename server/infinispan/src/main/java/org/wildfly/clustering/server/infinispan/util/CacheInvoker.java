/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.util;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import org.infinispan.Cache;
import org.wildfly.clustering.server.util.Invoker;

/**
 * @author Paul Ferraro
 */
public interface CacheInvoker extends Invoker {

	static Invoker retrying(Cache<?, ?> cache) {
		long timeout = cache.getCacheConfiguration().locking().lockAcquisitionTimeout();
		List<Duration> intervals = new LinkedList<>();
		// Generate exponential back-off intervals
		for (long interval = timeout; interval > 1; interval /= 10) {
			intervals.add(0, Duration.ofMillis(interval));
		}
		intervals.add(0, Duration.ZERO);
		return Invoker.retrying(intervals);
	}
}
