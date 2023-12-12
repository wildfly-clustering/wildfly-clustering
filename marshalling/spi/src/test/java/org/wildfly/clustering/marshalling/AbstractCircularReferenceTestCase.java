/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.test.Person;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractCircularReferenceTestCase {

	private final MarshallingTesterFactory factory;

	public AbstractCircularReferenceTestCase(MarshallingTesterFactory factory) {
		this.factory = factory;
	}

	@Test
	public void test() throws IOException {
		Person parent = Person.create("parent");
		Person self = Person.create("self");
		parent.addChild(self);
		parent.addChild(Person.create("sibling"));
		self.addChild(Person.create("son"));
		self.addChild(Person.create("daughter"));

		Tester<Person> tester = this.factory.createTester();
		tester.test(self, (expected, actual) -> {
			assertEquals(expected, actual);
			assertEquals(expected.getParent(), actual.getParent());
			assertEquals(expected.getChildren(), actual.getChildren());
			// Validate referential integrity
			for (Person child : actual.getParent().getChildren()) {
				assertSame(actual.getParent(), child.getParent());
			}
			for (Person child : actual.getChildren()) {
				assertSame(actual, child.getParent());
			}
		});
	}
}
