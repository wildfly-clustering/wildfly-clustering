/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.jgroups;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import org.jgroups.Address;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * @author Paul Ferraro
 */
public class AddressTestCase {

	@ParameterizedTest
	@TesterFactorySource
	private void test(TesterFactory factory) throws UnknownHostException {
		Consumer<Address> tester = factory.createTester();
		tester.accept(UUID.randomUUID());
		tester.accept(new IpAddress(InetAddress.getLocalHost(), Short.MAX_VALUE));
	}
}
