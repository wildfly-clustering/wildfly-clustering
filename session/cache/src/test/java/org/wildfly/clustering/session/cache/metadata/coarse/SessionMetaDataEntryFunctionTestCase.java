/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.coarse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.mockito.Mockito;
import org.wildfly.clustering.cache.Key;
import org.wildfly.common.function.Functions;


/**
 * Unit test for {@link SessionMetaDataEntryFunction}.
 * @author Paul Ferraro
 */
public class SessionMetaDataEntryFunctionTestCase extends AbstractSessionMetaDataEntryTestCase {

	@Override
	public void accept(ContextualSessionMetaDataEntry<Object> entry) {
		Object context = UUID.randomUUID();
		assertSame(context, entry.getContext(Functions.constantSupplier(context)));
		assertSame(context, entry.getContext(Functions.constantSupplier(null)));

		MutableSessionMetaDataOffsetValues delta = MutableSessionMetaDataOffsetValues.from(entry);

		MutableSessionMetaDataEntry mutableEntry = new MutableSessionMetaDataEntry(entry, delta);

		this.updateState(mutableEntry);

		this.verifyOriginalState(entry);

		Key<String> key = Mockito.mock(Key.class);

		ContextualSessionMetaDataEntry<Object> resultEntry = new SessionMetaDataEntryFunction<>(delta).apply(key, entry);

		Mockito.verifyNoInteractions(key);

		this.verifyUpdatedState(resultEntry);

		assertSame(context, resultEntry.getContext(Functions.constantSupplier(null)));
	}
}
