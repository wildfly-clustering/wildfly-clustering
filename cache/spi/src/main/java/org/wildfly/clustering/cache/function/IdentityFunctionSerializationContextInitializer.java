/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Function;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * Registers marshallers for identity functions for use with Cache.compute(...) operations.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class IdentityFunctionSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public IdentityFunctionSerializationContextInitializer() {
		super(Function.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(ProtoStreamMarshaller.of(BiFunction.FORMER_IDENTITY));
		context.registerMarshaller(ProtoStreamMarshaller.of(BiFunction.LATTER_IDENTITY));
		context.registerMarshaller(ProtoStreamMarshaller.of(Function.IDENTITY));
	}
}
