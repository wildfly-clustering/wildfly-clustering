/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.attributes;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

public class SessionAttributesKeyTestCase {

	@Test
	public void test() throws IOException {
		ProtoStreamTesterFactory.INSTANCE.createTester().test(new SessionAttributesKey("test"));
	}
}
