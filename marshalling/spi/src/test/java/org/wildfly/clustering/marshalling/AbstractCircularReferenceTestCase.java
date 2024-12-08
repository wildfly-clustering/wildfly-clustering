/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;

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
	public void test() {
		Person parent = Person.create("parent");
		Person self = Person.create("self");
		parent.addChild(self);
		parent.addChild(Person.create("sibling"));
		self.addChild(Person.create("son"));
		self.addChild(Person.create("daughter"));

		Consumer<Person> tester = this.factory.createTester((expected, actual) -> {
			assertThat(actual).isEqualTo(expected);
			assertThat(actual.getParent()).isEqualTo(expected.getParent());
			assertThat(actual.getChildren()).isEqualTo(expected.getChildren());
			// Validate referential integrity
			for (Person child : actual.getParent().getChildren()) {
				assertThat(child.getParent()).isSameAs(actual.getParent());
			}
			for (Person child : actual.getChildren()) {
				assertThat(child.getParent()).isSameAs(actual);
			}
		});
		tester.accept(self);
	}
}
