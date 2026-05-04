/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.transaction;

import java.util.UUID;

import javax.transaction.xa.Xid;

import org.infinispan.client.hotrod.transaction.manager.RemoteXid;
import org.infinispan.commons.tx.XidImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.Tester;
import org.wildfly.clustering.marshalling.TesterFactory;
import org.wildfly.clustering.marshalling.junit.TesterFactorySource;

/**
 * Unit test for {@link XidImplMarshaller}.
 * @author Paul Ferraro
 */
public class XidImplMarshallerTestCase {

	@ParameterizedTest
	@TesterFactorySource(MarshallingTesterFactory.class)
	public void test(TesterFactory factory) {
		Tester<Xid> tester = factory.createTester();

		Xid id = RemoteXid.create(UUID.randomUUID());
		tester.accept(id);
		tester.accept(XidImpl.copy(id));
	}
}
