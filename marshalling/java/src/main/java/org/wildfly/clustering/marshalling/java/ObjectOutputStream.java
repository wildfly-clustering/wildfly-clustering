/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.clustering.marshalling.java;

import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.wildfly.clustering.marshalling.Serializer;

/**
 * An {@link java.io.ObjectInputStream} that annotates classes using a given {@link Serializer}.
 * @author Paul Ferraro
 */
public class ObjectOutputStream extends java.io.ObjectOutputStream {

	private final Serializer<ClassLoader> serializer;

	/**
	 * Creates an object output stream decorator using the specified class loader serializer
	 * @param output the output input stream
	 * @param serializer a class loader serializer
	 * @throws IOException if the object input stream could not be created
	 */
	public ObjectOutputStream(OutputStream output, Serializer<ClassLoader> serializer) throws IOException {
		super(output);
		this.serializer = serializer;
	}

	@Override
	protected void annotateClass(Class<?> targetClass) throws IOException {
		this.serializer.write(this, getClassLoader(targetClass));
	}

	@Override
	protected void annotateProxyClass(Class<?> proxyClass) throws IOException {
		for (Class<?> interfaceClass : proxyClass.getInterfaces()) {
			this.serializer.write(this, getClassLoader(interfaceClass));
		}
		this.serializer.write(this, getClassLoader(proxyClass));
	}

	@SuppressWarnings("removal")
	private static ClassLoader getClassLoader(Class<?> targetClass) {
		if (System.getSecurityManager() == null) {
			return targetClass.getClassLoader();
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return targetClass.getClassLoader();
			}
		});
	}
}
