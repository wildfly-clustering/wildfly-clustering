/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.expiration;

import java.util.Map;

import org.wildfly.clustering.server.expiration.ExpirationMetaData;
import org.wildfly.clustering.server.infinispan.scheduler.ScheduleCommand;

/**
 * {@link ScheduleCommand} that wraps expiration metadata with a marshallable implementation.
 * @author Paul Ferraro
 * @param <K> the scheduled entry key type
 */
public class ScheduleExpirationCommand<K> extends ScheduleCommand<K, ExpirationMetaData> {

	/**
	 * Creates a schedule command for the specified entry.
	 * @param entry the scheduled entry
	 */
	public ScheduleExpirationCommand(Map.Entry<K, ExpirationMetaData> entry) {
		super(entry);
	}

	@Override
	protected ExpirationMetaData getValue() {
		return new SimpleExpirationMetaData(super.getValue());
	}
}
