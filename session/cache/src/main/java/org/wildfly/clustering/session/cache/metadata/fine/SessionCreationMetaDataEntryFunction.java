/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;

import org.wildfly.clustering.cache.function.RemappingFunction;
import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.server.offset.Offset;
import org.wildfly.clustering.server.offset.OffsetValue;

/**
 * @param <C> the session context type
 * @author Paul Ferraro
 */
public class SessionCreationMetaDataEntryFunction<C> extends RemappingFunction<SessionCreationMetaDataEntry<C>, java.util.function.Supplier<Offset<Duration>>> {

	public SessionCreationMetaDataEntryFunction(OffsetValue<Duration> operand) {
		super(operand::getOffset);
	}

	public SessionCreationMetaDataEntryFunction(Offset<Duration> operand) {
		super(Supplier.of(operand));
	}

	Offset<Duration> getOffset() {
		return this.getOperand().get();
	}
}
