/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.session.cache.metadata.fine;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mockito;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;

/**
 * Unit test for {@link CompositeSessionMetaData}.
 * @author Paul Ferraro
 */
public class CompositeSessionMetaDataTestCase extends AbstractImmutableSessionMetaDataTestCase {

	static class Parameters implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			SessionCreationMetaData creationMetaData = mock(SessionCreationMetaData.class);
			SessionAccessMetaData accessMetaData = mock(SessionAccessMetaData.class);
			Runnable mutator = mock(Runnable.class);
			InvalidatableSessionMetaData metaData = new CompositeSessionMetaData(creationMetaData, accessMetaData, mutator);
			return Stream.of(Arguments.of(creationMetaData, accessMetaData, mutator, metaData));
		}
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void isExpired(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.isExpired(creationMetaData, accessMetaData, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getCreationTime(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.getCreationTime(creationMetaData, accessMetaData, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getLastAccessStartTime(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.getLastAccessStartTime(creationMetaData, accessMetaData, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getLastAccessEndTime(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.getLastAccessEndTime(creationMetaData, accessMetaData, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getMaxInactiveInterval(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.getMaxIdle(creationMetaData, accessMetaData, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void setLastAccessed(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		// New session
		Instant creationTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		Instant startTime = creationTime;
		Instant endTime = startTime.plus(Duration.ofMillis(500));

		when(creationMetaData.getCreationTime()).thenReturn(creationTime);

		metaData.setLastAccess(startTime, endTime);

		// Request duration in second precision
		verify(accessMetaData).setLastAccessDuration(Duration.ZERO, Duration.ofSeconds(1L));
		verifyNoInteractions(mutator);

		reset(accessMetaData);

		// Existing session
		Duration sinceCreated = Duration.ofSeconds(10L);
		startTime = creationTime.plus(sinceCreated);
		endTime = startTime.plus(Duration.ofMillis(500));

		metaData.setLastAccess(startTime, endTime);

		verify(accessMetaData).setLastAccessDuration(sinceCreated, Duration.ofSeconds(1L));
		verifyNoInteractions(mutator);

		reset(accessMetaData);

		// Zero duration request
		sinceCreated = Duration.ofSeconds(20L);
		startTime = creationTime.plus(sinceCreated);
		endTime = startTime;

		metaData.setLastAccess(startTime, endTime);

		verify(accessMetaData).setLastAccessDuration(sinceCreated, Duration.ofSeconds(1L));
		verifyNoInteractions(mutator);
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void setMaxInactiveInterval(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		Duration duration = Duration.ZERO;

		metaData.setMaxIdle(duration);

		verify(creationMetaData).setMaxIdle(duration);
		verifyNoInteractions(mutator);
	}

	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void close(SessionCreationMetaData creationMetaData, SessionAccessMetaData accessMetaData, Runnable mutator, InvalidatableSessionMetaData metaData) {
		metaData.close();

		verify(mutator).run();
	}
}
