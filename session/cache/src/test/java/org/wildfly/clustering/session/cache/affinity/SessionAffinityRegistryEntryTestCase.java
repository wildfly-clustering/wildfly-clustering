/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class SessionAffinityRegistryEntryTestCase {

	@Test
	public void test() {
		TesterFactory factory = new ProtoStreamTesterFactory(new SessionAffinitySerializationContextInitializer());
		factory.createTester().accept(new SessionAffinityRegistryEntry("foo"));
	}
}
