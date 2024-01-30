/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote.user;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * Unit test for {@link UserContextKey}.
 * @author Paul Ferraro
 */
public class UserContextKeyTestCase {

	@Test
	public void test() throws IOException {
		ProtoStreamTesterFactory.INSTANCE.createTester().test(new UserContextKey("ABC123"));
	}
}
