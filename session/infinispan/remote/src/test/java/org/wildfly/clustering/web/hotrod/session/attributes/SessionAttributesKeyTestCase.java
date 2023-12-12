/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.web.hotrod.session.attributes;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.infinispan.remote.attributes.SessionAttributesKey;

public class SessionAttributesKeyTestCase {

	@Test
	public void test() throws IOException {
		new ProtoStreamTesterFactory().createTester().test(new SessionAttributesKey("test"));
	}
}
