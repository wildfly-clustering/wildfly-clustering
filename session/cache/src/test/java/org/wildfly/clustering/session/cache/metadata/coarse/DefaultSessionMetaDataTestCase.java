/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedInvocationConstants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.wildfly.clustering.server.offset.OffsetValue;
import org.wildfly.clustering.session.cache.metadata.InvalidatableSessionMetaData;

/**
 * @author Paul Ferraro
 */
public class DefaultSessionMetaDataTestCase extends AbstractImmutableSessionMetaDataTestCase {

	static class Parameters implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
			MutableSessionMetaDataEntry entry = mock(MutableSessionMetaDataEntry.class);
			Runnable mutator = mock(Runnable.class);
			InvalidatableSessionMetaData metaData = new DefaultSessionMetaData(entry, mutator);
			return Stream.of(Arguments.of(entry, mutator, metaData));
		}
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void testCreationTime(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.testCreationTime(entry, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void testLastAccessStartTime(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.testLastAccessStartTime(entry, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void testLastAccessEndTime(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.testLastAccessEndTime(entry, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void testTimeout(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		super.testTimeout(entry, metaData);
		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void setLastAccess(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		Instant endTime = Instant.now();
		Instant startTime = endTime.minus(Duration.ofMillis(500));
		OffsetValue<Instant> lastAccessStartTime = Mockito.mock(OffsetValue.class);
		OffsetValue<Instant> lastAccessEndTime = Mockito.mock(OffsetValue.class);

		ArgumentCaptor<Instant> lastAccessStartTimeCaptor = ArgumentCaptor.forClass(Instant.class);
		ArgumentCaptor<Instant> lastAccessEndTimeCaptor = ArgumentCaptor.forClass(Instant.class);

		doReturn(lastAccessStartTime).when(entry).getLastAccessStartTime();
		doReturn(lastAccessEndTime).when(entry).getLastAccessEndTime();

		doNothing().when(lastAccessStartTime).set(lastAccessStartTimeCaptor.capture());
		doNothing().when(lastAccessEndTime).set(lastAccessEndTimeCaptor.capture());

		metaData.setLastAccess(startTime, endTime);

		Instant normalizedStartTime = lastAccessStartTimeCaptor.getValue();
		Instant normalizedEndTime = lastAccessEndTimeCaptor.getValue();

		// Verify millisecond precision
		assertThat(normalizedStartTime.getNano() % Duration.ofMillis(1).getNano()).isZero();
		assertThat(normalizedStartTime.toEpochMilli()).isEqualTo(startTime.toEpochMilli());

		// Verify second precision
		Duration lastAccessDuration = Duration.between(normalizedStartTime, normalizedEndTime);
		assertThat(lastAccessDuration).hasSeconds(1);
		assertThat(lastAccessDuration.getNano()).isZero();

		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void setLastAccessZeroDuration(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		// Validate zero duration request
		Instant endTime = Instant.now();
		Instant startTime = endTime.minus(Duration.ofMillis(500));
		OffsetValue<Instant> lastAccessStartTime = Mockito.mock(OffsetValue.class);
		OffsetValue<Instant> lastAccessEndTime = Mockito.mock(OffsetValue.class);

		ArgumentCaptor<Instant> lastAccessStartTimeCaptor = ArgumentCaptor.forClass(Instant.class);
		ArgumentCaptor<Instant> lastAccessEndTimeCaptor = ArgumentCaptor.forClass(Instant.class);

		doReturn(lastAccessStartTime).when(entry).getLastAccessStartTime();
		doReturn(lastAccessEndTime).when(entry).getLastAccessEndTime();

		doNothing().when(lastAccessStartTime).set(lastAccessStartTimeCaptor.capture());
		doNothing().when(lastAccessEndTime).set(lastAccessEndTimeCaptor.capture());

		metaData.setLastAccess(startTime, endTime);

		Instant normalizedStartTime = lastAccessStartTimeCaptor.getValue();
		Instant normalizedEndTime = lastAccessEndTimeCaptor.getValue();

		// Verify millisecond precision
		assertThat(normalizedStartTime.getNano() % Duration.ofMillis(1).getNano()).isZero();
		assertThat(normalizedStartTime.toEpochMilli()).isEqualTo(startTime.toEpochMilli());

		// Verify second precision
		Duration lastAccessDuration = Duration.between(normalizedStartTime, normalizedEndTime);
		assertThat(lastAccessDuration).hasSeconds(1);
		assertThat(lastAccessDuration.getNano()).isZero();

		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void setTimeout(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		Duration timeout = Duration.ofHours(1);

		metaData.setTimeout(timeout);

		Mockito.verify(entry).setTimeout(timeout);

		Mockito.verifyNoInteractions(mutator);
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void invalidate(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		assertThat(metaData.isValid()).isTrue();

		metaData.invalidate();

		Mockito.verifyNoInteractions(entry);
		Mockito.verifyNoInteractions(mutator);

		assertThat(metaData.isValid()).isFalse();
	}

	@ParameterizedTest(name = ParameterizedInvocationConstants.INDEX_PLACEHOLDER)
	@ArgumentsSource(Parameters.class)
	public void close(MutableSessionMetaDataEntry entry, Runnable mutator, InvalidatableSessionMetaData metaData) {
		metaData.close();

		Mockito.verifyNoInteractions(entry);
		Mockito.verify(mutator).run();
	}
}
