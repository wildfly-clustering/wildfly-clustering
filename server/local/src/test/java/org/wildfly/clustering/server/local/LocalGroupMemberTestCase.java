/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.local;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;

/**
 * Unit test for {@link LocalNodeFormatter}.
 * @author Paul Ferraro
 */
public class LocalGroupMemberTestCase {
	private final DefaultLocalGroupMember localMember = new DefaultLocalGroupMember("name");

	@Test
	public void test() throws IOException {
		this.test(new FormatterTester<>(new DefaultLocalGroupMemberFormatter()));
//		this.test(JBossMarshallingTesterFactory.INSTANCE.createTester());
//		this.test(ProtoStreamTesterFactory.INSTANCE.createTester());
	}

	private void test(Tester<DefaultLocalGroupMember> tester) throws IOException {
		tester.test(this.localMember);
	}
}
