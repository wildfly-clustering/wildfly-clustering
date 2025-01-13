/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Paul Ferraro
 */
public interface Tester extends Consumer<List<Deployment>>, AutoCloseable {

	@Override
	void close();
}
