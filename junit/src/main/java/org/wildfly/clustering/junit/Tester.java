/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.junit;

import java.util.List;
import java.util.function.Consumer;

import org.wildfly.clustering.arquillian.Deployment;

/**
 * Encapsulates an Arquillian tester.
 * @author Paul Ferraro
 */
public interface Tester extends Consumer<List<Deployment>>, AutoCloseable {

	@Override
	default void close() {
	}
}
