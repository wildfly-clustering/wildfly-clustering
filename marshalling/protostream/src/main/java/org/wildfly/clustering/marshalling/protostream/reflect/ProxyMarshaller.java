/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.OptionalInt;
import java.util.function.Function;

import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamSizeOperation;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;
import org.wildfly.clustering.marshalling.protostream.Scalar;

/**
 * Marshaller for proxies serialized using the writeReplace()/readResolve() pattern.
 * @author Paul Ferraro
 */
public class ProxyMarshaller<T> implements ProtoStreamMarshaller<T> {
	private final ProtoStreamMarshaller<T> marshaller;

	public ProxyMarshaller(Class<? extends T> proxyClass) {
		this.marshaller = Scalar.ANY.toMarshaller(proxyClass, new Function<>() {
			@Override
			public Object apply(T object) {
				Method method = Reflect.findMethod(object.getClass(), "writeReplace");
				return Reflect.invoke(object, method);
			}
		}, new Function<>() {
			@Override
			public T apply(Object proxy) {
				Method method = Reflect.findMethod(proxy.getClass(), "readResolve");
				return Reflect.invoke(proxy, method, proxyClass);
			}
		});
	}

	@Override
	public OptionalInt size(ProtoStreamSizeOperation operation, T value) {
		return this.marshaller.size(operation, value);
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.marshaller.getJavaClass();
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		return this.marshaller.readFrom(reader);
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T value) throws IOException {
		this.marshaller.writeTo(writer, value);
	}
}
