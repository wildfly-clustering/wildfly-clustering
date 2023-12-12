/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.KeyMapperTester;
import org.wildfly.clustering.marshalling.FormatterTester;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class LocalModeAddressTestCase {

	@Test
	public void test() throws IOException {
		test(new FormatterTester<>(new LocalAddressSerializer.LocalAddressFormatter()));
		test(new KeyMapperTester<>(new KeyMapper()));
		test(new ProtoStreamTesterFactory().createTester());
	}

	private static void test(Tester<Address> tester) throws IOException {
		tester.test(LocalModeAddress.INSTANCE);
	}
}
