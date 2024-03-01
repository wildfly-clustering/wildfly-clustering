/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.net.URI;

/**
 * @author Paul Ferraro
 */
public interface ClientTester extends AutoCloseable {

	void test(URI baseURI1, URI baseURI2);

	@Override
	void close();
}
