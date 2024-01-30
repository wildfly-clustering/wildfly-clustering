/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.metadata;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link SessionAccessMetaDataKeyResolver}.
 * @author Paul Ferraro
 */
public class SessionMetaDataKeyTestCase {

	@Test
	public void test() throws IOException {
		ProtoStreamTesterFactory.INSTANCE.createTester().test(new SessionAccessMetaDataKey("test"));
		ProtoStreamTesterFactory.INSTANCE.createTester().test(new SessionCreationMetaDataKey("test"));
	}
}
