/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link UserContextKey}.
 * @author Paul Ferraro
 */
public class UserContextKeyTestCase {

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void test(TesterFactory factory) {
		factory.createTester().accept(new UserContextKey("ABC123"));
	}
}
