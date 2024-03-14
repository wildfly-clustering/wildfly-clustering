/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.TwoWayKey2StringMapperTesterFactory;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * Unit test for {@link DefaultLocalGroupMemberFormatter}.
 * @author Paul Ferraro
 */
public class LocalGroupMemberTestCase {

	@org.junit.jupiter.api.Disabled
	@ParameterizedTest
	@TesterFactorySource({ MarshallingTesterFactory.class, TwoWayKey2StringMapperTesterFactory.class })
	public void test(TesterFactory factory) {
		Consumer<DefaultLocalGroupMember> tester = factory.createTester();
		tester.accept(new DefaultLocalGroupMember("name"));
	}
}
