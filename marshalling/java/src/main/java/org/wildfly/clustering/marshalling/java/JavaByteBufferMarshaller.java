/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Serializable;

import org.wildfly.clustering.marshalling.AbstractByteBufferMarshaller;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.Serializer;

/**
 * A {@link ByteBufferMarshaller} based on Java serialization.
 * @author Paul Ferraro
 */
public class JavaByteBufferMarshaller extends AbstractByteBufferMarshaller {
	private final Serializer<ClassLoader> classLoaderSerializer;
	private final ObjectInputFilter filter;

	public JavaByteBufferMarshaller(ClassLoader loader, ObjectInputFilter filter) {
		this(Serializer.of(loader), loader, filter);
	}

	public JavaByteBufferMarshaller(Serializer<ClassLoader> classLoaderSerializer, ClassLoader loader, ObjectInputFilter filter) {
		super(loader);
		this.classLoaderSerializer = classLoaderSerializer;
		this.filter = filter;
	}

	@Override
	public boolean isMarshallable(Object object) {
		return (object == null) || object instanceof Serializable;
	}

	@Override
	public Object readFrom(InputStream in) throws IOException {
		try (ObjectInputStream input = new ObjectInputStream(in, this.classLoaderSerializer)) {
			if (this.filter != null) {
				input.setObjectInputFilter(this.filter);
			}
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

	@Override
	public String toString() {
		return "JavaSerialization";
	}
}
