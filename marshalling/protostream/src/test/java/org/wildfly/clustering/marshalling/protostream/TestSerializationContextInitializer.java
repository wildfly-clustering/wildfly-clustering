/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.test.TestComparator;
import org.wildfly.clustering.marshalling.test.TestInvocationHandler;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class TestSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public TestSerializationContextInitializer() {
		super("org.wildfly.clustering.marshalling.test.proto");
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(ProtoStreamMarshaller.of(new TestComparator<>()));
		context.registerMarshaller(Scalar.ANY.toMarshaller(TestInvocationHandler.class, TestInvocationHandler::getValue, TestInvocationHandler::new));
		context.registerMarshaller(new PersonMarshaller());
	}
}
