/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.user;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.TwoWayKey2StringMapperTesterFactory;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link UserSessionsKey}.
 * @author Paul Ferraro
 */
public class UserSessionsKeyTestCase {

	@ParameterizedTest
	@TesterFactorySource({ ProtoStreamTesterFactory.class, TwoWayKey2StringMapperTesterFactory.class })
	public void test(TesterFactory factory) {
		factory.createTester().accept(new UserSessionsKey("ABC123"));
	}
}
