/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util.concurrent.atomic;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.Scalar;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;

/**
 * @author Paul Ferraro
 */
public class AtomicSerializationContextInitializer extends AbstractSerializationContextInitializer {

	public AtomicSerializationContextInitializer() {
		super(AtomicReference.class.getPackage());
	}

	@Override
	public void registerMarshallers(SerializationContext context) {
		context.registerMarshaller(Scalar.BOOLEAN.cast(Boolean.class).toMarshaller(AtomicBoolean.class, AtomicBoolean::get, AtomicBoolean::new, AtomicBoolean::new));
		context.registerMarshaller(Scalar.INTEGER.cast(Integer.class).toMarshaller(AtomicInteger.class, AtomicInteger::get, AtomicInteger::new, AtomicInteger::new));
		context.registerMarshaller(Scalar.LONG.cast(Long.class).toMarshaller(AtomicLong.class, AtomicLong::get, AtomicLong::new, AtomicLong::new));
		context.registerMarshaller(Scalar.ANY.toMarshaller(AtomicReference.class, AtomicReference::get, AtomicReference::new, AtomicReference::new));
	}
}
