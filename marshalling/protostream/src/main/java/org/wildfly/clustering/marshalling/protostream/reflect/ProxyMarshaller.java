/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.OptionalInt;
import java.util.function.Function;

import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamSizeOperation;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;
import org.wildfly.clustering.marshalling.protostream.Scalar;

/**
 * Marshaller for proxies serialized using the writeReplace()/readResolve() pattern.
 * @param <T> the proxy class type
 * @author Paul Ferraro
 */
public class ProxyMarshaller<T> implements ProtoStreamMarshaller<T> {
	private final ProtoStreamMarshaller<T> marshaller;

	/**
	 * Creates a marshaller for a proxy of the specified type
	 * @param proxyClass the proxy class
	 */
	public ProxyMarshaller(Class<T> proxyClass) {
		this.marshaller = Scalar.ANY.toMarshaller(proxyClass, new Function<>() {
			@Override
			public Object apply(T object) {
				MethodHandle method = Reflect.getMethodHandle(object.getClass(), "writeReplace", MethodType.methodType(Object.class));
				try {
					return method.invokeExact(object);
				} catch (RuntimeException | Error e) {
					throw e;
				} catch (Throwable e) {
					throw new IllegalStateException(e);
				}
			}
		}, new Function<>() {
			@Override
			public T apply(Object proxy) {
				MethodHandle method = Reflect.getMethodHandle(proxy.getClass(), "readResolve", MethodType.methodType(Object.class));
				try {
					return (T) method.invokeExact(proxy);
				} catch (RuntimeException | Error e) {
					throw e;
				} catch (Throwable e) {
					throw new IllegalStateException(e);
				}
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
