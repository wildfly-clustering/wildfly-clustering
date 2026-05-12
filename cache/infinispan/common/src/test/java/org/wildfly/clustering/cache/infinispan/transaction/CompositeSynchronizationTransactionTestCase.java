/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;

import javax.transaction.xa.XAResource;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

/**
 * Unit test for {@link CompositeSynchronizationTransaction}.
 * @author Paul Ferraro
 */
public class CompositeSynchronizationTransactionTestCase {
	private final Random random = new Random();
	private final Transaction tx = mock(Transaction.class);
	private final Transaction subject = new CompositeSynchronizationTransaction(this.tx);

	@Test
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
		this.subject.commit();

		verify(this.tx).commit();
		verifyNoMoreInteractions(this.tx);
	}

	@Test
	public void delistResource() throws SystemException {
		XAResource resource = mock(XAResource.class);
		int flag = this.random.nextInt();
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(this.tx).delistResource(resource, flag);

		assertThat(this.subject.delistResource(resource, flag)).isEqualTo(expected);

		verify(this.tx).delistResource(resource, flag);
	}

	@Test
	public void enlistResource() throws RollbackException, SystemException {
		XAResource resource = mock(XAResource.class);
		boolean expected = this.random.nextBoolean();

		doReturn(expected).when(this.tx).enlistResource(resource);

		assertThat(this.subject.enlistResource(resource)).isEqualTo(expected);

		verify(this.tx).enlistResource(resource);
	}

	@Test
	public void getStatus() throws SystemException {
		int expected = this.random.nextInt();

		doReturn(expected).when(this.tx).getStatus();

		assertThat(this.subject.getStatus()).isEqualTo(expected);

		verify(this.tx).getStatus();
	}

	@Test
	public void registerSynchronization() throws RollbackException, SystemException {
		Synchronization synchronization1 = mock(Synchronization.class);
		Synchronization synchronization2 = mock(Synchronization.class);
		Synchronization synchronization3 = mock(Synchronization.class);

		ArgumentCaptor<Synchronization> capturedSynchronization = ArgumentCaptor.forClass(Synchronization.class);

		this.subject.registerSynchronization(synchronization1);

		verify(this.tx).registerSynchronization(capturedSynchronization.capture());

		this.subject.registerSynchronization(synchronization2);

		verifyNoMoreInteractions(this.tx);

		this.subject.registerSynchronization(synchronization3);

		verifyNoMoreInteractions(this.tx);

		Synchronization synchronization = capturedSynchronization.getValue();

		InOrder order = inOrder(synchronization1, synchronization2, synchronization3);

		synchronization.beforeCompletion();

		order.verify(synchronization1).beforeCompletion();
		order.verify(synchronization2).beforeCompletion();
		order.verify(synchronization3).beforeCompletion();
		order.verifyNoMoreInteractions();

		synchronization.afterCompletion(0);

		order.verify(synchronization3).afterCompletion(0);
		order.verify(synchronization2).afterCompletion(0);
		order.verify(synchronization1).afterCompletion(0);
		order.verifyNoMoreInteractions();
	}

	@Test
	public void rollback() throws IllegalStateException, SystemException {
		this.subject.rollback();

		verify(this.tx).rollback();
	}

	@Test
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		this.subject.setRollbackOnly();

		verify(this.tx).setRollbackOnly();
	}
}
