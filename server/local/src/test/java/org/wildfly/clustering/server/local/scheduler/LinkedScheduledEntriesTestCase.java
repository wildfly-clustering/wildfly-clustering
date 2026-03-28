/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.util.function.UnaryOperator;

/**
 * Unit test for a queued {@link ScheduledEntries} implementation.
 * @author Paul Ferraro
 */
public class LinkedScheduledEntriesTestCase extends AbstractScheduledEntriesTestCase {

	public LinkedScheduledEntriesTestCase() {
		super(ScheduledEntries.queued(), UnaryOperator.identity());
	}
}
