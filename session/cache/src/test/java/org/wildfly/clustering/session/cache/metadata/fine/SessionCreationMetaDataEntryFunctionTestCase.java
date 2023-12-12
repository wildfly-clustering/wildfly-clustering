/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import static org.junit.jupiter.api.Assertions.*;

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
		assertSame(context, entry.getContext().get(Functions.constantSupplier(context)));
		assertSame(context, entry.getContext().get(Functions.constantSupplier(null)));

		OffsetValue<Duration> timeoutOffset = OffsetValue.from(entry.getTimeout());

		MutableSessionCreationMetaData mutableEntry = new MutableSessionCreationMetaData(entry, timeoutOffset);

		this.updateState(mutableEntry);

		this.verifyOriginalState(entry);

		Key<String> key = Mockito.mock(Key.class);

		SessionCreationMetaDataEntry<Object> resultEntry = new SessionCreationMetaDataEntryFunction<>(timeoutOffset).apply(key, entry);

		Mockito.verifyNoInteractions(key);

		this.verifyUpdatedState(resultEntry);

		assertSame(context, resultEntry.getContext().get(Functions.constantSupplier(null)));
	}
}
