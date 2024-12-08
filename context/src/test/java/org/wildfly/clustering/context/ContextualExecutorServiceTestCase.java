/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.context;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author Paul Ferraro
 */
public class ContextualExecutorServiceTestCase {

	private final ExecutorService executor = mock(ExecutorService.class);
	private final Contextualizer contextualizer = mock(Contextualizer.class);
	private final ExecutorService subject = new ContextualExecutorService(this.executor, this.contextualizer);

	@AfterEach
	public void after() {
		reset(this.executor, this.contextualizer);
	}

	@Test
	public void execute() {
		Runnable command = mock(Runnable.class);
		Runnable contextualCommand = mock(Runnable.class);

		when(this.contextualizer.contextualize(same(command))).thenReturn(contextualCommand);

		this.subject.execute(command);

		verify(this.executor).execute(contextualCommand);
	}

	@Test
	public void shutdown() {
		this.subject.shutdown();

		verify(this.executor).shutdown();
	}

	@Test
	public void shutdownNow() {
		List<Runnable> expected = Collections.singletonList(mock(Runnable.class));

		when(this.executor.shutdownNow()).thenReturn(expected);

		List<Runnable> result = this.subject.shutdownNow();

		assertThat(result).isSameAs(expected);
	}

	@Test
	public void isShutdown() {
		when(this.executor.isShutdown()).thenReturn(false, true);

		assertThat(this.subject.isShutdown()).isFalse();
		assertThat(this.subject.isShutdown()).isTrue();
	}

	@Test
	public void isTerminated() {
		when(this.executor.isTerminated()).thenReturn(false, true);

		assertThat(this.subject.isTerminated()).isFalse();
		assertThat(this.subject.isTerminated()).isTrue();
	}

	@Test
	public void awaitTermination() throws InterruptedException {
		when(this.executor.awaitTermination(10L, TimeUnit.MINUTES)).thenReturn(false, true);

		assertThat(this.subject.awaitTermination(10L, TimeUnit.MINUTES)).isFalse();
		assertThat(this.subject.awaitTermination(10L, TimeUnit.MINUTES)).isTrue();
	}

	@Test
	public void submitCallable() {
		Callable<Object> task = mock(Callable.class);
		Callable<Object> contextualTask = mock(Callable.class);
		Future<Object> expected = mock(Future.class);

		when(this.contextualizer.contextualize(task)).thenReturn(contextualTask);
		when(this.executor.submit(same(contextualTask))).thenReturn(expected);

		Future<Object> result = this.subject.submit(task);

		assertThat(result).isSameAs(expected);
	}

	@Test
	public void submitRunnableWithResult() {
		Runnable task = mock(Runnable.class);
		Runnable contextualTask = mock(Runnable.class);
		Future<Object> expected = mock(Future.class);
		Object param = new Object();

		when(this.contextualizer.contextualize(task)).thenReturn(contextualTask);
		when(this.executor.submit(same(contextualTask), same(param))).thenReturn(expected);

		Future<Object> result = this.subject.submit(task, param);

		assertThat(result).isSameAs(expected);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void submit() {
		Runnable task = mock(Runnable.class);
		Runnable contextualTask = mock(Runnable.class);
		@SuppressWarnings("rawtypes")
		Future expected = mock(Future.class);

		when(this.contextualizer.contextualize(task)).thenReturn(contextualTask);
		when(this.executor.submit(same(contextualTask))).thenReturn(expected);

		Future<?> result = this.subject.submit(task);

		assertThat(result).isSameAs(expected);
	}

	@Test
	public void invokeAll() throws InterruptedException {
		Callable<Object> task1 = mock(Callable.class);
		Callable<Object> task2 = mock(Callable.class);
		Callable<Object> task3 = mock(Callable.class);
		Callable<Object> contextualTask1 = mock(Callable.class);
		Callable<Object> contextualTask2 = mock(Callable.class);
		Callable<Object> contextualTask3 = mock(Callable.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Callable<Object>>> capturedTasks = ArgumentCaptor.forClass(List.class);
		@SuppressWarnings("unchecked")
		List<Future<Object>> expected = Collections.singletonList(mock(Future.class));

		when(this.contextualizer.contextualize(task1)).thenReturn(contextualTask1);
		when(this.contextualizer.contextualize(task2)).thenReturn(contextualTask2);
		when(this.contextualizer.contextualize(task3)).thenReturn(contextualTask3);
		when(this.executor.invokeAll(capturedTasks.capture())).thenReturn(expected);

		List<Future<Object>> result = this.subject.invokeAll(Arrays.asList(task1, task2, task3));

		assertThat(result).isSameAs(expected);

		assertThat(capturedTasks.getValue()).isNotNull().containsExactly(contextualTask1, contextualTask2, contextualTask3);
	}

	@Test
	public void invokeAllWithTimeout() throws InterruptedException {
		Callable<Object> task1 = mock(Callable.class);
		Callable<Object> task2 = mock(Callable.class);
		Callable<Object> task3 = mock(Callable.class);
		Callable<Object> contextualTask1 = mock(Callable.class);
		Callable<Object> contextualTask2 = mock(Callable.class);
		Callable<Object> contextualTask3 = mock(Callable.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Callable<Object>>> capturedTasks = ArgumentCaptor.forClass(List.class);
		@SuppressWarnings("unchecked")
		List<Future<Object>> expected = Collections.singletonList(mock(Future.class));

		when(this.contextualizer.contextualize(task1)).thenReturn(contextualTask1);
		when(this.contextualizer.contextualize(task2)).thenReturn(contextualTask2);
		when(this.contextualizer.contextualize(task3)).thenReturn(contextualTask3);
		when(this.executor.invokeAll(capturedTasks.capture(), eq(10L), same(TimeUnit.MINUTES))).thenReturn(expected);

		List<Future<Object>> result = this.subject.invokeAll(Arrays.asList(task1, task2, task3), 10L, TimeUnit.MINUTES);

		assertThat(result).isSameAs(expected);

		assertThat(capturedTasks.getValue()).isNotNull().containsExactly(contextualTask1, contextualTask2, contextualTask3);
	}

	@Test
	public void invokeAny() throws InterruptedException, ExecutionException {
		Callable<Object> task1 = mock(Callable.class);
		Callable<Object> task2 = mock(Callable.class);
		Callable<Object> task3 = mock(Callable.class);
		Callable<Object> contextualTask1 = mock(Callable.class);
		Callable<Object> contextualTask2 = mock(Callable.class);
		Callable<Object> contextualTask3 = mock(Callable.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Callable<Object>>> capturedTasks = ArgumentCaptor.forClass(List.class);
		Object expected = new Object();

		when(this.contextualizer.contextualize(task1)).thenReturn(contextualTask1);
		when(this.contextualizer.contextualize(task2)).thenReturn(contextualTask2);
		when(this.contextualizer.contextualize(task3)).thenReturn(contextualTask3);
		when(this.executor.invokeAny(capturedTasks.capture())).thenReturn(expected);

		Object result = this.subject.invokeAny(Arrays.asList(task1, task2, task3));

		assertThat(result).isSameAs(expected);

		assertThat(capturedTasks.getValue()).isNotNull().containsExactly(contextualTask1, contextualTask2, contextualTask3);
	}

	@Test
	public void invokeAnyWithTimeout() throws InterruptedException, ExecutionException, TimeoutException {
		Callable<Object> task1 = mock(Callable.class);
		Callable<Object> task2 = mock(Callable.class);
		Callable<Object> task3 = mock(Callable.class);
		Callable<Object> contextualTask1 = mock(Callable.class);
		Callable<Object> contextualTask2 = mock(Callable.class);
		Callable<Object> contextualTask3 = mock(Callable.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Callable<Object>>> capturedTasks = ArgumentCaptor.forClass(List.class);
		Object expected = new Object();

		when(this.contextualizer.contextualize(task1)).thenReturn(contextualTask1);
		when(this.contextualizer.contextualize(task2)).thenReturn(contextualTask2);
		when(this.contextualizer.contextualize(task3)).thenReturn(contextualTask3);
		when(this.executor.invokeAny(capturedTasks.capture(), eq(10L), same(TimeUnit.MINUTES))).thenReturn(expected);

		Object result = this.subject.invokeAny(Arrays.asList(task1, task2, task3), 10L, TimeUnit.MINUTES);

		assertThat(result).isSameAs(expected);

		assertThat(capturedTasks.getValue()).isNotNull().containsExactly(contextualTask1, contextualTask2, contextualTask3);
	}
}
