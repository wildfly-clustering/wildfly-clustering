/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.List;

import org.wildfly.clustering.arquillian.Deployment;

/**
 * @author Paul Ferraro
 */
public interface ClientTester extends AutoCloseable {

	void test(List<Deployment> deployments);

	@Override
	void close();
}
