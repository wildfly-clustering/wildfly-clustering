/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleCommand;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleWithPersistentMetaDataCommand;

/**
 * {@link ScheduleCommand} that wraps expiration metadata with a marshallable implementation.
 * @author Paul Ferraro
 * @param <I> the scheduled entry identifier type
 */
public class ScheduleWithExpirationMetaDataCommand<I> extends ScheduleWithPersistentMetaDataCommand<I, ExpirationMetaData> {

	/**
	 * Creates a schedule command for the cache entry associated with the specified identifier.
	 * @param id the identifier of a scheduled item
	 * @param metaData a scheduled entry metadata
	 */
	public ScheduleWithExpirationMetaDataCommand(I id, ExpirationMetaData metaData) {
		super(id, metaData);
	}

	@Override
	protected ExpirationMetaData getMetaData() {
		return new SimpleExpirationMetaData(super.getMetaData());
	}
}
