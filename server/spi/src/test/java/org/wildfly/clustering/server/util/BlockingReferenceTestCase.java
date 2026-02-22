/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Validates the linearisability of concurrent read/write operations against a reference.
 * @author Paul Ferraro
 */
public class BlockingReferenceTestCase {
	private static final int ITERATIONS = 1000;
	private static final int CONCURRENCY = 100;

	@Test
	public void test() throws ExecutionException {
		int expected = 0;
		Random random = new Random();
		BlockingReference<Integer> reference = BlockingReference.of(Integer.valueOf(0));
		BlockingReference.Writer<Integer> writer = reference.getWriter();
		List<Runnable> tasks = new ArrayList<>(ITERATIONS);
		for (int i = 0; i < ITERATIONS; ++i) {
			int increment = random.nextInt(0, 10);
			expected += increment;
			tasks.add(() -> writer.write(value -> value + increment));
		}
		ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
		try {
			List<Future<?>> futures = new ArrayList<>(ITERATIONS);
			for (Runnable task : tasks) {
				futures.add(executor.submit(task));
			}
			for (Future<?> future : futures) {
				future.get();
			}
			Assertions.assertThat(reference.getReader().read(Function.identity())).isEqualTo(expected);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			executor.shutdown();
		}
	}
}
