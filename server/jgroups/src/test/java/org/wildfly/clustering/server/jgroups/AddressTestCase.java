/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.io.IOException;
import java.net.InetAddress;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.junit.jupiter.api.Test;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class AddressTestCase {

	@Test
	public void test() throws IOException {
		test(new ProtoStreamTesterFactory().createTester());
	}

	private static void test(Tester<Address> tester) throws IOException {
		tester.test(UUID.randomUUID());
		tester.test(new IpAddress(IpAddressMarshaller.DEFAULT_ADDRESS, IpAddressMarshaller.DEFAULT_PORT));
		tester.test(new IpAddress(InetAddress.getLocalHost(), Short.MAX_VALUE));
	}
}
