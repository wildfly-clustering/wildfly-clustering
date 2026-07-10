/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang.invoke;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.FieldMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;
import org.wildfly.clustering.marshalling.protostream.Scalar;

/**
 * ProtoStream marshaller for a serializable lambda.
 * @author Paul Ferraro
 */
public enum LambdaMarshaller implements FieldMarshaller<Object> {
	/** Singleton instance */
	INSTANCE;

	@Override
	public Object readFrom(ProtoStreamReader reader) throws IOException {
		SerializedLambda lambda = Scalar.ANY.cast(SerializedLambda.class).readFrom(reader);
		MethodHandle deserializeLambda = Privileged.getMethodHandle(SerializedLambdaMarshaller.getCapturingClass(lambda), "$deserializeLambda$", MethodType.methodType(Object.class, SerializedLambda.class));
		try {
			return deserializeLambda.invoke(lambda);
		} catch (Throwable e) {
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Object synthetic) throws IOException {
		MethodHandle writeReplace = Privileged.getMethodHandle(synthetic.getClass(), "writeReplace", MethodType.methodType(Object.class));
		try {
			SerializedLambda lambda = (SerializedLambda) writeReplace.invoke(synthetic);
			Scalar.ANY.writeTo(writer, lambda);
		} catch (Throwable e) {
			if (e instanceof IOException) {
				throw (IOException) e;
			}
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Class<?> getJavaClass() {
		return Object.class;
	}

	@Override
	public WireType getWireType() {
		return WireType.LENGTH_DELIMITED;
	}
}
