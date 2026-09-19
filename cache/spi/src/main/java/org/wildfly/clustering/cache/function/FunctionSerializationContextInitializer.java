/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.function;

import java.util.function.Function;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;

/**
 * The serialization context initializer for this package.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class FunctionSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a serialization context initializer.
	 */
	public FunctionSerializationContextInitializer() {
		// Do nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(MapComputeFunctionMarshaller.INSTANCE);
		context.registerMarshaller(new CollectionFunctionMarshaller<>(SetAddFunction.class, SetAddFunction::new));
		context.registerMarshaller(new CollectionFunctionMarshaller<>(SetRemoveFunction.class, SetRemoveFunction::new));
		context.registerMarshaller(Scalar.ANY.toMarshaller(RemappingFunction.class, RemappingFunction::getOperand, RemappingFunction::new));
		context.registerMarshaller(Scalar.ANY.cast((Class<Function<? super Object, ? extends Object>>) (Class<?>) Function.class).toMarshaller(ComputeIfAbsentFunction.class, ComputeIfAbsentFunction::getDefaultValueFactory, ComputeIfAbsentFunction::new));
		context.registerMarshaller(Scalar.ANY.toMarshaller(PutIfAbsentFunction.class, PutIfAbsentFunction::getDefaultValue, PutIfAbsentFunction::new));
	}
}
