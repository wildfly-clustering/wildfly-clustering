/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.cache;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.manager.Service;

/**
 * Unit test for shared and unshared context implementations.
 * @author Paul Ferraro
 */
public class CacheTestCase {

	private static final int KEYS = 10;
	private static final int SIZE = 100;

	@Test
	public void concurrent() throws InterruptedException, ExecutionException {
		Cache<Integer, ManagedService<Integer>> manager = CacheStrategy.CONCURRENT.createCache(Service::start, Service::stop);
		List<Future<ManagedService<Integer>>> futures = new ArrayList<>(KEYS * SIZE);
		ExecutorService executor = Executors.newFixedThreadPool(KEYS);
		try {
			for (int i = 0; i < KEYS; ++i) {
				List<Callable<ManagedService<Integer>>> tasks = new ArrayList<>(SIZE);
				for (int j = 0; j < SIZE; ++j) {
					int key = i;
					tasks.add(() -> {
						try (ManagedService<Integer> object = manager.computeIfAbsent(key, ManagedService::new)) {
							assertThat(object.isStarted()).as(object::toString).isTrue();
							assertThat(object.isStopped()).as(object::toString).isFalse();
							// Emulate usage of shared object
							Thread.sleep(10);
							return object;
						}
					});
				}
				for (Callable<ManagedService<Integer>> task : tasks) {
					futures.add(executor.submit(task));
				}
			}
			// Wait until all tasks are finished
			for (Future<ManagedService<Integer>> future : futures) {
				future.get();
			}
			// Verify
			for (Future<ManagedService<Integer>> future : futures) {
				ManagedService<Integer> object = future.get();
				assertThat(object.isStarted()).as(object::toString).isTrue();
				assertThat(object.isStopped()).as(object::toString).isTrue();
			}
		} finally {
			executor.shutdown();
		}
	}

	@Test
	public void none() {
		Cache<String, ManagedService<String>> context = CacheStrategy.NONE.createCache(Service::start, Service::stop);
		@SuppressWarnings("resource")
		ManagedService<String> object = context.computeIfAbsent("foo", ManagedService::new);
		try {
			assertThat(object.isStarted()).isTrue();
			assertThat(object.isStopped()).isFalse();

			try (ManagedService<String> object2 = context.computeIfAbsent("foo", ManagedService::new)) {
				assertThat(object2).isNotSameAs(object);
				assertThat(object2.isStarted()).isTrue();
				assertThat(object2.isStopped()).isFalse();
			}
		} finally {
			assertThat(object.isStarted()).isTrue();
			assertThat(object.isStopped()).isFalse();

			object.close();

			assertThat(object.isStarted()).isTrue();
			assertThat(object.isStopped()).isTrue();
		}
	}

	static class ManagedService<I> implements Service, AutoCloseable {
		private volatile boolean started = false;
		private volatile boolean stopped = false;
		private final I id;
		private final Runnable closeTask;

		ManagedService(I id, Runnable closeTask) {
			this.id = id;
			this.closeTask = closeTask;
		}

		public I getId() {
			return this.id;
		}

		@Override
		public boolean isStarted() {
			return this.started;
		}

		@Override
		public void start() {
			// Emulate some starting cost
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			this.started = true;
		}

		@Override
		public void stop() {
			// Emulate some stopping cost
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			this.stopped = true;
		}

		boolean isStopped() {
			return this.stopped;
		}

		@Override
		public void close() {
			this.closeTask.run();
		}

		@Override
		public String toString() {
			return this.id.toString();
		}
	}
}
