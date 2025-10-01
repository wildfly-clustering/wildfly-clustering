/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.wildfly.clustering.marshalling.Serializer;

/**
 * An {@link java.io.ObjectInputStream} that resolves classes using a given {@link Serializer}.
 * @author Paul Ferraro
 */
public class ObjectInputStream extends java.io.ObjectInputStream {
	private static final InvocationHandler NULL_HANDLER = (proxy, method, args) -> null;

	private final Serializer<ClassLoader> serializer;

	/**
	 * Creates an object input stream decorator using the specified class loader serializer
	 * @param input the decorated input stream
	 * @param serializer a class loader serializer
	 * @throws IOException if the object input stream could not be created
	 */
	public ObjectInputStream(InputStream input, Serializer<ClassLoader> serializer) throws IOException {
		super(input);
		this.serializer = serializer;
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass description) throws IOException, ClassNotFoundException {
		String className = description.getName();
		return Class.forName(className, false, this.serializer.read(this));
	}

	@Override
	protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
		Class<?>[] interfaceClasses = new Class<?>[interfaces.length];
		for (int i = 0; i < interfaces.length; ++i) {
			interfaceClasses[i] = Class.forName(interfaces[i], false, this.serializer.read(this));
		}
		try {
			return Proxy.newProxyInstance(this.serializer.read(this), interfaceClasses, NULL_HANDLER).getClass();
		} catch (IllegalArgumentException e) {
			throw new ClassNotFoundException(null, e);
		}
	}
}
