/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.math;

import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.wildfly.clustering.function.Supplier;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * Serialization context initializer for the {@link java.math} package.
 * @author Paul Ferraro
 */
public class MathSerializationContextInitializer extends AbstractSerializationContextInitializer {

	/**
	 * Creates a new serialization context initializer.
	 */
	public MathSerializationContextInitializer() {
		super(MathContext.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(BigDecimalMarshaller.INSTANCE);
		context.registerMarshaller(Scalar.BYTE_ARRAY.cast(byte[].class).toMarshaller(BigInteger.class, BigInteger::toByteArray, Supplier.of(BigInteger.ZERO), BigInteger::new));
		context.registerMarshaller(MathContextMarshaller.INSTANCE);
		context.registerMarshaller(ProtoStreamMarshaller.of(RoundingMode.class));
	}
}
