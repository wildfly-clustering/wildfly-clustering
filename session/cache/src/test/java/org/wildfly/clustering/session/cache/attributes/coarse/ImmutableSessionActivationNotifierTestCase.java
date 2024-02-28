/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.attributes.coarse;

import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionAttributes;
import org.wildfly.clustering.session.container.SessionActivationListenerFacadeProvider;

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

	private final SessionActivationListenerFacadeProvider<Session, Context, Listener> provider = mock(SessionActivationListenerFacadeProvider.class);
	private final ImmutableSession session = mock(ImmutableSession.class);
	private final Context context = mock(Context.class);
	private final Listener listener1 = mock(Listener.class);
	private final Listener listener2 = mock(Listener.class);

	private final SessionActivationNotifier notifier = new ImmutableSessionActivationNotifier<>(this.provider, this.session, this.context);

	@AfterEach
	public void destroy() {
		Mockito.reset(this.session, this.provider);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		ImmutableSessionAttributes attributes = mock(ImmutableSessionAttributes.class);

		when(this.session.getAttributes()).thenReturn(attributes);
		when(attributes.getAttributeNames()).thenReturn(Set.of("foo", "bar", "listener1", "listener2"));
		when(attributes.getAttribute("foo")).thenReturn(UUID.randomUUID());
		when(attributes.getAttribute("bar")).thenReturn(UUID.randomUUID());
		when(attributes.getAttribute("listener1")).thenReturn(this.listener1);
		when(attributes.getAttribute("listener2")).thenReturn(this.listener2);

		when(this.provider.asSessionActivationListener(any())).thenReturn(Optional.empty());
		when(this.provider.asSessionActivationListener(this.listener1)).thenReturn(Optional.of(this.listener1));
		when(this.provider.asSessionActivationListener(this.listener2)).thenReturn(Optional.of(this.listener2));

		Session session = mock(Session.class);
		Consumer<Session> prePassivateNotifier1 = mock(Consumer.class);
		Consumer<Session> prePassivateNotifier2 = mock(Consumer.class);
		Consumer<Session> postActivateNotifier1 = mock(Consumer.class);
		Consumer<Session> postActivateNotifier2 = mock(Consumer.class);

		when(this.provider.asSession(same(this.session), same(this.context))).thenReturn(session);
		when(this.provider.prePassivateNotifier(same(this.listener1))).thenReturn(prePassivateNotifier1);
		when(this.provider.prePassivateNotifier(same(this.listener2))).thenReturn(prePassivateNotifier2);
		when(this.provider.postActivateNotifier(same(this.listener1))).thenReturn(postActivateNotifier1);
		when(this.provider.postActivateNotifier(same(this.listener2))).thenReturn(postActivateNotifier2);

		// verify pre-passivate before post-activate is a no-op
		this.notifier.prePassivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		// verify initial post-activate
		this.notifier.postActivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1).accept(session);
		verify(postActivateNotifier2).accept(session);

		reset(postActivateNotifier1, postActivateNotifier2);

		// verify subsequent post-activate is a no-op
		this.notifier.postActivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		// verify pre-passivate following post-activate
		this.notifier.prePassivate();

		verify(prePassivateNotifier1).accept(session);
		verify(prePassivateNotifier2).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		reset(prePassivateNotifier1, prePassivateNotifier2);

		// verify subsequent pre-passivate is a no-op
		this.notifier.prePassivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1, never()).accept(session);
		verify(postActivateNotifier2, never()).accept(session);

		// verify post-activate following pre-passivate
		this.notifier.postActivate();

		verify(prePassivateNotifier1, never()).accept(session);
		verify(prePassivateNotifier2, never()).accept(session);
		verify(postActivateNotifier1).accept(session);
		verify(postActivateNotifier2).accept(session);
	}

	@Test
	public void postActivate() {
		ImmutableSessionAttributes attributes = mock(ImmutableSessionAttributes.class);
		UUID foo = UUID.randomUUID();
		UUID bar = UUID.randomUUID();

		when(this.session.getAttributes()).thenReturn(attributes);
		when(attributes.getAttributeNames()).thenReturn(Set.of("foo", "bar", "listener1", "listener2"));
		when(attributes.getAttribute("foo")).thenReturn(UUID.randomUUID());
		when(attributes.getAttribute("bar")).thenReturn(UUID.randomUUID());
		when(attributes.getAttribute("listener1")).thenReturn(this.listener1);
		when(attributes.getAttribute("listener2")).thenReturn(this.listener2);
		when(this.provider.asSessionActivationListener(foo)).thenReturn(Optional.empty());
		when(this.provider.asSessionActivationListener(bar)).thenReturn(Optional.empty());
		when(this.provider.asSessionActivationListener(this.listener1)).thenReturn(Optional.of(this.listener1));
		when(this.provider.asSessionActivationListener(this.listener2)).thenReturn(Optional.of(this.listener2));

		Session session = mock(Session.class);
		Consumer<Session> notifier1 = mock(Consumer.class);
		Consumer<Session> notifier2 = mock(Consumer.class);

		when(this.provider.asSession(same(this.session), same(this.context))).thenReturn(session);
		when(this.provider.postActivateNotifier(same(this.listener1))).thenReturn(notifier1);
		when(this.provider.postActivateNotifier(same(this.listener2))).thenReturn(notifier2);

		this.notifier.postActivate();

		verify(this.provider, never()).prePassivateNotifier(this.listener1);
		verify(this.provider, never()).prePassivateNotifier(this.listener2);

		verify(notifier1).accept(session);
		verify(notifier2).accept(session);
	}
}
