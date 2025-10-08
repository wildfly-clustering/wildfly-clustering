/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.CacheProperties;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.util.Supplied;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;
import org.wildfly.clustering.session.Session;
import org.wildfly.clustering.session.cache.attributes.SessionAttributes;
import org.wildfly.clustering.session.cache.attributes.SessionAttributesFactory;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;
import org.wildfly.clustering.session.cache.metadata.SessionMetaDataFactory;

/**
 * Unit test for {@link CompositeSessionFactory}.
 *
 * @author Paul Ferraro
 */
public class CompositeSessionFactoryTestCase {
	private final SessionMetaDataFactory<Contextual<Object>> metaDataFactory = mock(SessionMetaDataFactory.class);
	private final SessionAttributesFactory<Object, Object> attributesFactory = mock(SessionAttributesFactory.class);
	private final CacheProperties properties = mock(CacheProperties.class);
	private final Object transientContext = new Object();

	private final SessionFactory<Object, Contextual<Object>, Object, Object> factory = new CompositeSessionFactory<>(new SessionFactoryConfiguration<>() {
		@Override
		public CacheProperties getCacheProperties() {
			return CompositeSessionFactoryTestCase.this.properties;
		}

		@Override
		public SessionMetaDataFactory<Contextual<Object>> getSessionMetaDataFactory() {
			return CompositeSessionFactoryTestCase.this.metaDataFactory;
		}

		@Override
		public SessionAttributesFactory<Object, Object> getSessionAttributesFactory() {
			return CompositeSessionFactoryTestCase.this.attributesFactory;
		}

		@Override
		public java.util.function.Supplier<Object> getSessionContextFactory() {
			return Supplier.of(CompositeSessionFactoryTestCase.this.transientContext);
		}
	});

	@Test
	public void createValue() {
		Contextual<Object> contextual = mock(Contextual.class);
		Object attributes = new Object();
		String id = "id";

		when(this.metaDataFactory.createValueAsync(id, null)).thenReturn(CompletableFuture.completedStage(contextual));
		when(this.attributesFactory.createValueAsync(id, null)).thenReturn(CompletableFuture.completedStage(attributes));

		Map.Entry<Contextual<Object>, Object> result = this.factory.createValue(id, null);

		assertThat(result).isNotNull();
		assertThat(result.getKey()).isSameAs(contextual);
		assertThat(result.getValue()).isSameAs(attributes);
	}

	@Test
	public void findValue() {
		String missingMetaDataSessionId = "no-meta-data";
		String missingAttributesSessionId = "no-attributes";
		String existingSessionId = "existing";
		Contextual<Object> contextual = mock(Contextual.class);
		Object attributes = new Object();

		when(this.metaDataFactory.findValueAsync(missingMetaDataSessionId)).thenReturn(CompletableFuture.completedStage(null));
		when(this.metaDataFactory.findValueAsync(missingAttributesSessionId)).thenReturn(CompletableFuture.completedStage(contextual));
		when(this.metaDataFactory.findValueAsync(existingSessionId)).thenReturn(CompletableFuture.completedStage(contextual));
		when(this.attributesFactory.findValueAsync(missingMetaDataSessionId)).thenReturn(CompletableFuture.completedStage(attributes));
		when(this.attributesFactory.findValueAsync(missingAttributesSessionId)).thenReturn(CompletableFuture.completedStage(null));
		when(this.attributesFactory.findValueAsync(existingSessionId)).thenReturn(CompletableFuture.completedStage(attributes));

		Map.Entry<Contextual<Object>, Object> missingMetaDataResult = this.factory.findValue(missingMetaDataSessionId);
		Map.Entry<Contextual<Object>, Object> missingAttributesResult = this.factory.findValue(missingAttributesSessionId);
		Map.Entry<Contextual<Object>, Object> existingSessionResult = this.factory.findValue(existingSessionId);

		assertThat(missingMetaDataResult).isNull();
		assertThat(missingAttributesResult).isNull();
		assertThat(existingSessionResult).isNotNull();
		assertThat(existingSessionResult.getKey()).isSameAs(contextual);
		assertThat(existingSessionResult.getValue()).isSameAs(attributes);
	}

	@Test
	public void remove() {
		String id = "id";

		when(this.metaDataFactory.removeAsync(id)).thenReturn(CompletableFuture.completedStage(null));
		when(this.attributesFactory.removeAsync(id)).thenReturn(CompletableFuture.completedStage(null));

		this.factory.removeAsync(id);

		verify(this.metaDataFactory).removeAsync(id);
		verify(this.attributesFactory).removeAsync(id);
	}

	@Test
	public void getMetaDataFactory() {
		assertThat(this.factory.getSessionMetaDataFactory()).isSameAs(this.metaDataFactory);
	}

	@Test
	public void createSession() {
		Contextual<Object> contextual = mock(Contextual.class);
		Object attributesValue = new Object();
		InvalidatableSessionMetaData metaData = mock(InvalidatableSessionMetaData.class);
		SessionAttributes attributes = mock(SessionAttributes.class);
		Object context = new Object();
		String id = "id";

		when(this.metaDataFactory.createSessionMetaData(id, contextual)).thenReturn(metaData);
		when(this.attributesFactory.createSessionAttributes(same(id), same(attributesValue), same(metaData), same(context))).thenReturn(attributes);
		when(contextual.getContext()).thenReturn(Supplied.simple());

		Session<Object> result = this.factory.createSession(id, Map.entry(contextual, attributesValue), context);

		assertThat(result.getId()).isSameAs(id);
		assertThat(result.getMetaData()).isSameAs(metaData);
		assertThat(result.getAttributes()).isSameAs(attributes);
		assertThat(result.getContext()).isSameAs(this.transientContext);
	}

	@Test
	public void createImmutableSession() {
		Contextual<Object> contextual = mock(Contextual.class);
		Object attributesValue = new Object();
		ImmutableSessionMetaData metaData = mock(ImmutableSessionMetaData.class);
		Map<String, Object> attributes = mock(Map.class);
		String id = "id";

		when(this.metaDataFactory.createImmutableSessionMetaData(id, contextual)).thenReturn(metaData);
		when(this.attributesFactory.createImmutableSessionAttributes(id, attributesValue)).thenReturn(attributes);

		ImmutableSession result = this.factory.createImmutableSession(id, Map.entry(contextual, attributesValue));

		assertThat(result.getId()).isSameAs(id);
		assertThat(result.getMetaData()).isSameAs(metaData);
		assertThat(result.getAttributes()).isSameAs(attributes);
	}

	@Test
	public void close() {
		this.factory.close();

		verify(this.metaDataFactory).close();
		verify(this.attributesFactory).close();
	}
}
