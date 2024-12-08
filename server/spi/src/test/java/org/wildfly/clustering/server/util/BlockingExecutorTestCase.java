/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.util;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.wildfly.common.function.ExceptionRunnable;
import org.wildfly.common.function.ExceptionSupplier;

/**
 * @author Paul Ferraro
 */
public class BlockingExecutorTestCase {

	@Test
	public void testExecuteRunnable() {
		Runnable closeTask = mock(Runnable.class);
		@SuppressWarnings("resource")
		BlockingExecutor executor = BlockingExecutor.newInstance(closeTask);

		Runnable executeTask = mock(Runnable.class);

		executor.execute(executeTask);

		// Task should run
		verify(executeTask).run();
		verify(closeTask, never()).run();
		reset(executeTask);

		executor.close();

		verify(closeTask).run();
		reset(closeTask);

		executor.close();

		// Close task should only run once
		verify(closeTask, never()).run();

		executor.execute(executeTask);

		// Task should no longer run since service is closed
		verify(executeTask, never()).run();
		verify(closeTask, never()).run();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteExceptionRunnable() throws Exception {
		Runnable closeTask = mock(Runnable.class);
		@SuppressWarnings("resource")
		BlockingExecutor executor = BlockingExecutor.newInstance(closeTask);

		ExceptionRunnable<Exception> executeTask = mock(ExceptionRunnable.class);

		executor.execute(executeTask);

		// Task should run
		verify(executeTask).run();
		verify(closeTask, never()).run();
		reset(executeTask);

		doThrow(new Exception()).when(executeTask).run();

		assertThatExceptionOfType(Exception.class).isThrownBy(() -> executor.execute(executeTask));

		verify(closeTask, never()).run();
		reset(executeTask);

		executor.close();

		verify(closeTask).run();
		reset(closeTask);

		executor.close();

		// Close task should only run once
		verify(closeTask, never()).run();

		executor.execute(executeTask);

		// Task should no longer run since service is closed
		verify(executeTask, never()).run();
		verify(closeTask, never()).run();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteSupplier() {
		Runnable closeTask = mock(Runnable.class);
		@SuppressWarnings("resource")
		BlockingExecutor executor = BlockingExecutor.newInstance(closeTask);
		Object expected = new Object();

		Supplier<Object> executeTask = mock(Supplier.class);

		when(executeTask.get()).thenReturn(expected);

		Optional<Object> result = executor.execute(executeTask);

		// Task should run
		assertThat(result).isPresent().containsSame(expected);
		verify(closeTask, never()).run();
		reset(executeTask);

		executor.close();

		verify(closeTask).run();
		reset(closeTask);

		executor.close();

		// Close task should only run once
		verify(closeTask, never()).run();
		verify(closeTask, never()).run();

		result = executor.execute(executeTask);

		// Task should no longer run since service is closed
		assertThat(result).isEmpty();
		verify(closeTask, never()).run();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecuteExceptionSupplier() throws Exception {
		Runnable closeTask = mock(Runnable.class);
		@SuppressWarnings("resource")
		BlockingExecutor executor = BlockingExecutor.newInstance(closeTask);
		Object expected = new Object();

		ExceptionSupplier<Object, Exception> executeTask = mock(ExceptionSupplier.class);

		when(executeTask.get()).thenReturn(expected);

		Optional<Object> result = executor.execute(executeTask);

		// Task should run
		assertThat(result).isPresent().containsSame(expected);
		verify(closeTask, never()).run();
		reset(executeTask);

		doThrow(new Exception()).when(executeTask).get();

		assertThatExceptionOfType(Exception.class).isThrownBy(() -> executor.execute(executeTask));

		verify(closeTask, never()).run();
		reset(executeTask);

		executor.close();

		verify(closeTask).run();
		reset(closeTask);

		executor.close();

		// Close task should only run once
		verify(closeTask, never()).run();

		result = executor.execute(executeTask);

		// Task should no longer run since service is closed
		assertThat(result).isEmpty();
		verify(closeTask, never()).run();
	}

	@Test
	public void concurrent() throws InterruptedException, ExecutionException {
		Runnable closeTask = mock(Runnable.class);
		BlockingExecutor executor = BlockingExecutor.newInstance(closeTask);

		ExecutorService service = Executors.newFixedThreadPool(2);
		try {
			CountDownLatch executeLatch = new CountDownLatch(1);
			CountDownLatch stopLatch = new CountDownLatch(1);
			Runnable executeTask = () -> {
				try {
					executeLatch.countDown();
					stopLatch.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			};
			Future<?> executeFuture = service.submit(() -> executor.execute(executeTask));

			executeLatch.await();

			Future<?> closeFuture = service.submit(executor::close);

			Thread.yield();

			// Verify that stop is blocked
			verify(closeTask, never()).run();

			stopLatch.countDown();

			executeFuture.get();
			closeFuture.get();

			// Verify close task was invoked, now that execute task is complete
			verify(closeTask).run();
		} finally {
			service.shutdownNow();
		}
	}
}
