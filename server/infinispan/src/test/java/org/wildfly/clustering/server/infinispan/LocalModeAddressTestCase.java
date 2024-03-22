/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.LocalModeAddress;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.FormatterTesterFactory;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class LocalModeAddressTestCase {

	@ParameterizedTest
	@TesterFactorySource({ ProtoStreamTesterFactory.class, FormatterTesterFactory.class })
	public void test(TesterFactory factory) {
		Tester<Address> tester = factory.createTester();
		tester.accept(LocalModeAddress.INSTANCE);
	}
}
