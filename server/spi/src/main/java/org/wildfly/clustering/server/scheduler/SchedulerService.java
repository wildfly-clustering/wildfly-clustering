/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.scheduler;

import java.util.Optional;
import java.util.function.Function;

import org.wildfly.clustering.server.service.Service;

/**
 * A restartable scheduler.
 * @param <K> the scheduled entry identifier type
 * @param <V> the scheduled entry value type
 * @author Paul Ferraro
 */
public interface SchedulerService<K, V> extends Scheduler<K, V>, Service, AutoCloseable {

	/**
	 * Returns a mapped scheduler.
	 * @param <KK> the mapped identifier type
	 * @param <VV> the mapped value type
	 * @param identifierMapper the identifier mapping function
	 * @param entryMapper the entry mapping function
	 * @return a mapped scheduler.
	 */
	default <KK, VV> SchedulerService<KK, VV> compose(Function<KK, K> identifierMapper, Function<VV, Optional<V>> entryMapper) {
		return new SchedulerService<>() {
			@Override
			public void schedule(KK id, VV value) {
				Optional<V> mapped = entryMapper.apply(value);
				if (mapped.isPresent()) {
					SchedulerService.this.schedule(identifierMapper.apply(id), mapped.get());
				}
			}

			@Override
			public void cancel(KK id) {
				SchedulerService.this.cancel(identifierMapper.apply(id));
			}

			@Override
			public boolean contains(KK id) {
				return SchedulerService.this.contains(identifierMapper.apply(id));
			}

			@Override
			public boolean isStarted() {
				return SchedulerService.this.isStarted();
			}

			@Override
			public void start() {
				SchedulerService.this.start();
			}

			@Override
			public void stop() {
				SchedulerService.this.stop();
			}

			@Override
			public void close() {
				SchedulerService.this.close();
			}
		};
	}

	@Override
	void close();
}
