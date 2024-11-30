/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.FileDescriptorSource;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractSerializationContextInitializer implements SerializationContextInitializer {

	private final String resourceName;
	private final ClassLoader loader;

	protected AbstractSerializationContextInitializer() {
		this.resourceName = this.getClass().getPackage().getName() + ".proto";
		this.loader = Privileged.getClassLoader(this.getClass());
	}

	protected AbstractSerializationContextInitializer(String resourceName) {
		this.resourceName = resourceName;
		this.loader = Privileged.getClassLoader(this.getClass());
	}

	protected AbstractSerializationContextInitializer(String resourceName, Class<?> containingClass) {
		this.resourceName = resourceName;
		this.loader = Privileged.getClassLoader(containingClass);
	}

	@Override
	public void registerSchema(SerializationContext context) {
		try {
			context.registerProtoFiles(getFileDescriptorSource(this.loader, this.resourceName));
		} catch (DescriptorParserException e) {
			try {
				// If parsing failed, unregister this schema so others can register
				context.unregisterProtoFile(this.resourceName);
			} catch (RuntimeException re) {
				// Ignore
			}
			throw e;
		}
	}

	@Override
	public String toString() {
		return this.resourceName;
	}

	private static FileDescriptorSource getFileDescriptorSource(ClassLoader loader, String resourceName) {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<>() {
				@Override
				public FileDescriptorSource run() throws Exception {
					return FileDescriptorSource.fromResources(loader, resourceName);
				}
			});
		} catch (PrivilegedActionException e) {
			Exception exception = e.getException();
			if (exception instanceof IOException) {
				throw new UncheckedIOException((IOException) exception);
			}
			throw new RuntimeException(exception);
		}
	}
}
