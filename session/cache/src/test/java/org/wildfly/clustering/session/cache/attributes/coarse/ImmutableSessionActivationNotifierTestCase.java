/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.session.container.ContainerProvider;

/**
 * @author Paul Ferraro
 */
public class ImmutableSessionActivationNotifierTestCase {
	interface Session {
	}
	interface Context {
	}
	interface Listener {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		ContainerProvider<Context, Session, Listener, Void> provider = mock(ContainerProvider.class);
		Session session = mock(Session.class);
		Listener listener1 = mock(Listener.class);
		Listener listener2 = mock(Listener.class);
		List<Object> attributes = List.of(UUID.randomUUID(), listener1, listener2);
		SessionActivationNotifier notifier = new ImmutableSessionActivationNotifier<>(provider, session, attributes);

		doReturn(Optional.empty()).when(provider).getSessionEventListener(same(session), any(UUID.class));
		doReturn(Optional.of(listener1)).when(provider).getSessionEventListener(same(session), same(listener1));
		doReturn(Optional.of(listener2)).when(provider).getSessionEventListener(same(session), same(listener2));

		Consumer<Session> prePassivateNotifier1 = mock(Consumer.class);
		Consumer<Session> prePassivateNotifier2 = mock(Consumer.class);
		Consumer<Session> postActivateNotifier1 = mock(Consumer.class);
		Consumer<Session> postActivateNotifier2 = mock(Consumer.class);

		doReturn(prePassivateNotifier1).when(provider).getPrePassivateEventNotifier(listener1);
		doReturn(prePassivateNotifier2).when(provider).getPrePassivateEventNotifier(listener2);
		doReturn(postActivateNotifier1).when(provider).getPostActivateEventNotifier(listener1);
		doReturn(postActivateNotifier2).when(provider).getPostActivateEventNotifier(listener2);

		// verify pre-passivate before post-activate is a no-op
		notifier.prePassivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		// verify initial post-activate
		notifier.postActivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1).accept(session);
		verify(postActivateNotifier2).accept(session);

		reset(postActivateNotifier1, postActivateNotifier2);

		// verify subsequent post-activate is a no-op
		notifier.postActivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		// verify pre-passivate following post-activate
		notifier.prePassivate();

		verify(prePassivateNotifier1).accept(session);
		verify(prePassivateNotifier2).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		reset(prePassivateNotifier1, prePassivateNotifier2);

		// verify subsequent pre-passivate is a no-op
		notifier.prePassivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		// verify post-activate following pre-passivate
		notifier.postActivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1).accept(session);
		verify(postActivateNotifier2).accept(session);
	}
}
