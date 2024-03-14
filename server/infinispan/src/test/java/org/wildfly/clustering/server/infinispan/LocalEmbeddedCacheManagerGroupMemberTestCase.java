/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.TwoWayKey2StringMapperTesterFactory;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * @author Paul Ferraro
 */
public class LocalEmbeddedCacheManagerGroupMemberTestCase {

	@ParameterizedTest
	@TesterFactorySource({ MarshallingTesterFactory.class, TwoWayKey2StringMapperTesterFactory.class })
	public void test(TesterFactory factory) {
		factory.createTester().accept(new LocalEmbeddedCacheManagerGroupMember("foo"));
	}
}
