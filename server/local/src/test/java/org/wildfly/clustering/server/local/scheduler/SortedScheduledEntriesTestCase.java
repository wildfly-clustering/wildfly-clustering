/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unit test for {@link SortedScheduledEntries}
 * @author Paul Ferraro
 */
public class SortedScheduledEntriesTestCase extends AbstractScheduledEntriesTestCase {

	public SortedScheduledEntriesTestCase() {
		super(ScheduledEntries.sorted(), list -> {
			List<Map.Entry<UUID, Instant>> result = new LinkedList<>(list);
			Collections.sort(result, SortedScheduledEntries.defaultComparator());
			return result;
		});
	}
}
