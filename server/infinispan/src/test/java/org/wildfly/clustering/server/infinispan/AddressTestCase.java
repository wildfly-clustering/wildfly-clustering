/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.NodeVersion;
import org.jgroups.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.FormatterTesterFactory;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class AddressTestCase {

	@ParameterizedTest
	@TesterFactorySource({ ProtoStreamTesterFactory.class, FormatterTesterFactory.class })
	public void test(TesterFactory factory) {
		Tester<Address> tester = factory.createTester();
		tester.accept(Address.LOCAL);
		tester.accept(Address.random("foo"));
		tester.accept(Address.random("foo", "site", null, null));
		tester.accept(Address.random("foo", null, "rack", null));
		tester.accept(Address.random("foo", null, null, "machine"));
		tester.accept(Address.random("foo", "site", "rack", null));
		tester.accept(Address.random("foo", "site", null, "machine"));
		tester.accept(Address.random("foo", null, "rack", "machine"));
		tester.accept(Address.random("foo", "site", "rack", "machine"));
		UUID id = UUID.randomUUID();
		tester.accept(Address.protoFactory(id.getMostSignificantBits(), id.getLeastSignificantBits(), NodeVersion.INSTANCE, "site", "rack", "machine"));
	}
}
