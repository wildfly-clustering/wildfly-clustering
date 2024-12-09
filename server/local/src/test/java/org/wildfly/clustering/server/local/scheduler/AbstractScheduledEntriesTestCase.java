/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local.scheduler;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractScheduledEntriesTestCase {

	private final ScheduledEntries<UUID, Instant> entrySet;
	private final UnaryOperator<List<Map.Entry<UUID, Instant>>> expectedFactory;

	AbstractScheduledEntriesTestCase(ScheduledEntries<UUID, Instant> entrySet, UnaryOperator<List<Map.Entry<UUID, Instant>>> expectedFactory) {
		this.entrySet = entrySet;
		this.expectedFactory = expectedFactory;
	}

	@Test
	public void test() {
		// Verify empty
		assertThat(this.entrySet).isEmpty();

		// Populate
		List<Map.Entry<UUID, Instant>> entries = new LinkedList<>();
		Instant now = Instant.now();
		entries.add(new SimpleImmutableEntry<>(UUID.randomUUID(), now));
		entries.add(new SimpleImmutableEntry<>(UUID.randomUUID(), now));
		entries.add(new SimpleImmutableEntry<>(UUID.randomUUID(), now.minus(Duration.ofSeconds(1))));
		entries.add(new SimpleImmutableEntry<>(UUID.randomUUID(), now.plus(Duration.ofSeconds(2))));
		entries.add(new SimpleImmutableEntry<>(UUID.randomUUID(), now.plus(Duration.ofSeconds(1))));

		for (Map.Entry<UUID, Instant> entry : entries) {
			this.entrySet.add(entry.getKey(), entry.getValue());
		}

		List<Map.Entry<UUID, Instant>> expected = this.expectedFactory.apply(entries);
		System.out.println("Actual:\t" + this.entrySet);
		System.out.println("Expected:\t" + expected);
		assertThat(this.entrySet).containsExactlyElementsOf(expected);

		// Verify iteration order after removal of first item
		this.entrySet.remove(expected.remove(0).getKey());
		assertThat(this.entrySet).containsExactlyElementsOf(expected);

		// Verify iteration order after removal of middle item
		this.entrySet.remove(expected.remove((expected.size() - 1) / 2).getKey());
		assertThat(this.entrySet).containsExactlyElementsOf(expected);

		// Verify iteration order after removal of last item
		this.entrySet.remove(expected.remove((expected.size() - 1)).getKey());
		assertThat(this.entrySet).containsExactlyElementsOf(expected);

		// Verify removal of non-existent entry
		this.entrySet.remove(UUID.randomUUID());
		assertThat(this.entrySet).containsExactlyElementsOf(expected);
	}
}
