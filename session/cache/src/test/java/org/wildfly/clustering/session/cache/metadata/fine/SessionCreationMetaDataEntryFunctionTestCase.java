/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.UUID;

import org.mockito.Mockito;
import org.wildfly.clustering.cache.Key;
import org.wildfly.clustering.server.offset.OffsetValue;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class SessionCreationMetaDataEntryFunctionTestCase extends AbstractSessionCreationMetaDataEntryTestCase {

	@Override
	public void accept(SessionCreationMetaDataEntry<Object> entry) {
		Object context = UUID.randomUUID();
		assertThat(entry.getContext().get(Functions.constantSupplier(context))).isSameAs(context);
		assertThat(entry.getContext().get(Functions.constantSupplier(null))).isSameAs(context);

		OffsetValue<Duration> timeoutOffset = OffsetValue.from(entry.getTimeout());

		MutableSessionCreationMetaData mutableEntry = new MutableSessionCreationMetaData(entry, timeoutOffset);

		this.updateState(mutableEntry);

		this.verifyOriginalState(entry);

		Key<String> key = Mockito.mock(Key.class);

		SessionCreationMetaDataEntry<Object> resultEntry = new SessionCreationMetaDataEntryFunction<>(timeoutOffset).apply(key, entry);

		Mockito.verifyNoInteractions(key);

		this.verifyUpdatedState(resultEntry);

		assertThat(resultEntry.getContext().get(Functions.constantSupplier(null))).isSameAs(context);
	}
}
