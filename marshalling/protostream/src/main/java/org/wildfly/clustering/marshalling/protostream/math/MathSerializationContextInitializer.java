/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.math;

import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.common.function.Functions;

/**
 * @author Paul Ferraro
 */
public class MathSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public MathSerializationContextInitializer() {
		super(MathContext.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(new BigDecimalMarshaller());
		context.registerMarshaller(Scalar.BYTE_ARRAY.cast(byte[].class).toMarshaller(BigInteger.class, BigInteger::toByteArray, Functions.constantSupplier(BigInteger.ZERO), BigInteger::new));
		context.registerMarshaller(new MathContextMarshaller());
		context.registerMarshaller(ProtoStreamMarshaller.of(RoundingMode.class));
	}
}
