/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

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
public class CompositeImmutableSessionMetaDataTestCase extends AbstractImmutableSessionMetaDataTestCase {

	static class Parameters implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			ImmutableSessionCreationMetaData creationMetaData = mock(ImmutableSessionCreationMetaData.class);
			ImmutableSessionAccessMetaData accessMetaData = mock(ImmutableSessionAccessMetaData.class);
			ImmutableSessionMetaData metaData = new CompositeImmutableSessionMetaData(creationMetaData, accessMetaData);
			return Stream.of(Arguments.of(creationMetaData, accessMetaData, metaData));
		}
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void isExpired(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		super.isExpired(creationMetaData, accessMetaData, metaData);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getCreationTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		super.getCreationTime(creationMetaData, accessMetaData, metaData);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getLastAccessStartTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		super.getLastAccessStartTime(creationMetaData, accessMetaData, metaData);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getLastAccessEndTime(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		super.getLastAccessEndTime(creationMetaData, accessMetaData, metaData);
	}

	@Override
	@ParameterizedTest
	@ArgumentsSource(Parameters.class)
	public void getMaxIdle(ImmutableSessionCreationMetaData creationMetaData, ImmutableSessionAccessMetaData accessMetaData, ImmutableSessionMetaData metaData) {
		super.getMaxIdle(creationMetaData, accessMetaData, metaData);
	}
}
