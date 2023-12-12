/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.web.hotrod.session.metadata;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionAccessMetaDataKey;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionCreationMetaDataKey;

/**
 * Unit test for {@link SessionAccessMetaDataKeyResolver}.
 * @author Paul Ferraro
 */
public class SessionMetaDataKeyTestCase {

	@Test
	public void test() throws IOException {
		ProtoStreamTesterFactory factory = new ProtoStreamTesterFactory();
		factory.createTester().test(new SessionAccessMetaDataKey("test"));
		factory.createTester().test(new SessionCreationMetaDataKey("test"));
	}
}
