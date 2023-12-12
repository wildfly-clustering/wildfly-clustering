/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.context;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.server.manager.Restartable;

/**
 * Unit test for shared and unshared context implementations.
 * @author Paul Ferraro
 */
public class ContextTestCase {

	private static final int KEYS = 10;
	private static final int SIZE = 100;

	@Test
	public void shared() throws InterruptedException, ExecutionException {
		Context<Integer, ManagedObject<Integer>> manager = ContextStrategy.SHARED.createContext(Restartable::start, Restartable::stop);
		List<List<Future<ManagedObject<Integer>>>> keyFutures = new ArrayList<>(KEYS);
		ExecutorService executor = Executors.newFixedThreadPool(KEYS);
		try {
			for (int i = 0; i < KEYS; ++i) {
				List<Future<ManagedObject<Integer>>> futures = new ArrayList<>(SIZE);
				keyFutures.add(futures);
				for (int j = 0; j < SIZE; ++j) {
					int key = i;
					Callable<ManagedObject<Integer>> task = () -> {
						try (ManagedObject<Integer> object = manager.computeIfAbsent(key, ManagedObject::new)) {
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
			for (List<Future<ManagedObject<Integer>>> futures : keyFutures) {
				for (Future<ManagedObject<Integer>> future : futures) {
					future.get();
				}
			}
			// Verify
			for (List<Future<ManagedObject<Integer>>> futures : keyFutures) {
				for (Future<ManagedObject<Integer>> future : futures) {
					ManagedObject<Integer> object = future.get();
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
		Context<String, ManagedObject<String>> context = ContextStrategy.UNSHARED.createContext(Restartable::start, Restartable::stop);
		@SuppressWarnings("resource")
		ManagedObject<String> object = context.computeIfAbsent("foo", ManagedObject::new);
		try {
			assertTrue(object.isStarted());
			assertFalse(object.isStopped());

			try (ManagedObject<String> object2 = context.computeIfAbsent("foo", ManagedObject::new)) {
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

	static class ManagedObject<I> implements Restartable, AutoCloseable {
		private volatile boolean started = false;
		private volatile boolean stopped = false;
		private final I id;
		private final Runnable closeTask;

		ManagedObject(I id, Runnable closeTask) {
			this.id = id;
			this.closeTask = closeTask;
		}

		public I getId() {
			return this.id;
		}

		@Override
		public void start() {
			this.started = true;
		}

		@Override
		public void stop() {
			this.stopped = true;
		}

		boolean isStarted() {
			return this.started;
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
