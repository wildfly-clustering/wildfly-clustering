/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan.provider;

import java.util.Set;
import java.util.function.Consumer;

import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.cache.function.AbstractCollectionOperationFunction;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamTesterFactory;

/**
 * @author Paul Ferraro
 */
public class AddressSetFunctionMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource(ProtoStreamTesterFactory.class)
	public void test(TesterFactory factory) {
		Consumer<AbstractCollectionOperationFunction<Address, Set<Address>>> tester = factory.createTester();

		Address address = Address.random();
		tester.accept(new AddressSetAddFunction(address));
		tester.accept(new AddressSetRemoveFunction(address));
	}
}
