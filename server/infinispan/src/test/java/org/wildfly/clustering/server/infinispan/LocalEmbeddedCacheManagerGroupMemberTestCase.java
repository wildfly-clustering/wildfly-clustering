/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.jboss.JBossTesterFactory;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class LocalEmbeddedCacheManagerGroupMemberTestCase {

	private final LocalEmbeddedCacheManagerGroupMember member = new LocalEmbeddedCacheManagerGroupMember("foo");

	@Test
	public void test() throws IOException {
		this.test(new FormatterTester<>(new LocalEmbeddedCacheManagerGroupMemberFormatter()));
		this.test(JBossTesterFactory.INSTANCE.createTester());
		this.test(new ProtoStreamTesterFactory().createTester());
	}

	public void test(Tester<LocalEmbeddedCacheManagerGroupMember> tester) throws IOException {
		tester.test(this.member);
	}
}
