/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.test.Empty;
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
		context.registerMarshaller(new ValueMarshaller<>(new TestComparator<>()));
		context.registerMarshaller(new EnumMarshaller<>(Empty.class));
		context.registerMarshaller(new FunctionalScalarMarshaller<>(TestInvocationHandler.class, Scalar.ANY, TestInvocationHandler::getValue, TestInvocationHandler::new));
		context.registerMarshaller(new PersonMarshaller());
	}
}
