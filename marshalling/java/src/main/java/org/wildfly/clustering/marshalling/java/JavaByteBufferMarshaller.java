/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.Serializer;

/**
 * A {@link ByteBufferMarshaller} based on Java serialization.
 * @author Paul Ferraro
 */
public class JavaByteBufferMarshaller implements ByteBufferMarshaller {
	private final Serializer<ClassLoader> classLoaderSerializer;

	public JavaByteBufferMarshaller() {
		this(Serializer.of(AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader())));
	}

	public JavaByteBufferMarshaller(Serializer<ClassLoader> classLoaderSerializer) {
		this.classLoaderSerializer = classLoaderSerializer;
	}

	@Override
	public boolean isMarshallable(Object object) {
		return (object == null) || object instanceof Serializable;
	}

	@Override
	public Object readFrom(InputStream in) throws IOException {
		try (ObjectInput input = new ObjectInputStream(in, this.classLoaderSerializer)) {
			return input.readObject();
		} catch (ClassNotFoundException e) {
			InvalidClassException exception = new InvalidClassException(e.getMessage());
			exception.initCause(e);
			throw exception;
		}
	}

	@Override
	public void writeTo(OutputStream out, Object object) throws IOException {
		try (ObjectOutput output = new ObjectOutputStream(out, this.classLoaderSerializer)) {
			output.writeObject(object);
		}
	}
}
