/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link SessionAccessMetaDataKey} and {@link SessionCreationMetaDataKey} marshalling.
 * @author Paul Ferraro
 */
public class SessionMetaDataKeyTestCase {

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void test(TesterFactory factory) {
		factory.createTester().accept(new SessionAccessMetaDataKey("test"));
		factory.createTester().accept(new SessionCreationMetaDataKey("test"));
	}
}
