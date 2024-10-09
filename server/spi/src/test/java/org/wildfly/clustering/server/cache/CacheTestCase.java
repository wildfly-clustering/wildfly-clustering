/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.cache;

import static org.junit.jupiter.api.Assertions.*;

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
	public void shared() throws InterruptedException, ExecutionException {
		Cache<Integer, ManagedService<Integer>> manager = CacheStrategy.CONCURRENT.createCache(Service::start, Service::stop);
		List<List<Future<ManagedService<Integer>>>> keyFutures = new ArrayList<>(KEYS);
		ExecutorService executor = Executors.newFixedThreadPool(KEYS);
		try {
			for (int i = 0; i < KEYS; ++i) {
				List<Future<ManagedService<Integer>>> futures = new ArrayList<>(SIZE);
				keyFutures.add(futures);
				for (int j = 0; j < SIZE; ++j) {
					int key = i;
					Callable<ManagedService<Integer>> task = () -> {
						try (ManagedService<Integer> object = manager.computeIfAbsent(key, ManagedService::new)) {
							assertTrue(object.isStarted());
							assertFalse(object.isStopped());
							Thread.sleep(10);
							return object;
						}
					};
					futures.add(executor.submit(task));
				}
			}
			// Wait until all tasks are finished
			for (List<Future<ManagedService<Integer>>> futures : keyFutures) {
				for (Future<ManagedService<Integer>> future : futures) {
					future.get();
				}
			}
			// Verify
			for (List<Future<ManagedService<Integer>>> futures : keyFutures) {
				for (Future<ManagedService<Integer>> future : futures) {
					ManagedService<Integer> object = future.get();
					assertTrue(object.isStarted(), object::toString);
					assertTrue(object.isStopped(), object::toString);
				}
			}
		} finally {
			executor.shutdown();
		}
	}

	@Test
	public void unshared() {
		Cache<String, ManagedService<String>> context = CacheStrategy.NONE.createCache(Service::start, Service::stop);
		@SuppressWarnings("resource")
		ManagedService<String> object = context.computeIfAbsent("foo", ManagedService::new);
		try {
			assertTrue(object.isStarted());
			assertFalse(object.isStopped());

			try (ManagedService<String> object2 = context.computeIfAbsent("foo", ManagedService::new)) {
				assertNotSame(object2, object);
				assertTrue(object2.isStarted());
				assertFalse(object2.isStopped());
			}
		} finally {
			assertTrue(object.isStarted());
			assertFalse(object.isStopped());

			object.close();

			assertTrue(object.isStarted());
			assertTrue(object.isStopped());
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
			this.started = true;
		}

		@Override
		public void stop() {
			this.stopped = true;
		}

		boolean isStopped() {
			return this.stopped;
		}

		@Override
		public void close() {
			this.closeTask.run();
		}
	}
}
