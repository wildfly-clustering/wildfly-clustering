/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.util.List;

import org.jboss.marshalling.ClassTable;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.test.Person;
import org.wildfly.clustering.marshalling.test.TestComparator;
import org.wildfly.clustering.marshalling.test.TestInvocationHandler;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(ClassTable.class)
public class TestClassTable extends IdentityClassTable {

	public TestClassTable() {
		super(List.of(Person.class, TestComparator.class, TestInvocationHandler.class));
	}
}
