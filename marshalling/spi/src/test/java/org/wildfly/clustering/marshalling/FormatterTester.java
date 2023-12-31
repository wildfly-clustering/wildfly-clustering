/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.Assertions;

/**
 * Tester for a {@link Formatter}.
 * @author Paul Ferraro
 */
public class FormatterTester<K> implements Tester<K> {

	private final Formatter<K> format;

	public FormatterTester(Formatter<K> format) {
		this.format = format;
	}

	@Override
	public void test(K key) {
		this.test(key, Assertions::assertEquals);
	}

	@Override
	public void test(K subject, BiConsumer<K, K> assertion) {
		assertTrue(this.format.getTargetClass().isInstance(subject));

		String formatted = this.format.format(subject);

		K result = this.format.parse(formatted);

		assertion.accept(subject, result);
	}
}
