/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * @author Paul Ferraro
 */
public class DefaultImmutableSessionMetaDataTestCase extends AbstractImmutableSessionMetaDataTestCase {

	static class Parameters implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			ImmutableSessionMetaDataEntry entry = mock(ImmutableSessionMetaDataEntry.class);
			ImmutableSessionMetaData metaData = new DefaultImmutableSessionMetaData(entry);
			return Stream.of(Arguments.of(entry, metaData));
		}
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void testCreationTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		super.testCreationTime(entry, metaData);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void testLastAccessStartTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		super.testLastAccessStartTime(entry, metaData);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void testLastAccessEndTime(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		super.testLastAccessEndTime(entry, metaData);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void testTimeout(ImmutableSessionMetaDataEntry entry, ImmutableSessionMetaData metaData) {
		super.testTimeout(entry, metaData);
	}
}
