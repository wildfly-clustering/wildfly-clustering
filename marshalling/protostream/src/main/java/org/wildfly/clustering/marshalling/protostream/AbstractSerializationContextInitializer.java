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
 * An abstract initializer of a serialization context handling location and registration of the protobuf schemas.
 * @author Paul Ferraro
 */
public abstract class AbstractSerializationContextInitializer implements SerializationContextInitializer {

	private final String resourceName;
	private final ClassLoader loader;

	/**
	 * Creates a new serialization context initializer that loads a protobuf schema file using the name of the package of this implementation class.
	 */
	protected AbstractSerializationContextInitializer() {
		this.resourceName = getResourceName(this.getClass().getPackage());
		this.loader = Privileged.getClassLoader(this.getClass());
	}

	/**
	 * Creates a new serialization context initializer that loads a protobuf schema file using the name of the specified package.
	 * @param targetPackage the package whose name corresponds to the protobuf schema file
	 */
	protected AbstractSerializationContextInitializer(Package targetPackage) {
		this.resourceName = getResourceName(targetPackage);
		this.loader = Privileged.getClassLoader(this.getClass());
	}

	/**
	 * Creates a new serialization context initializer that loads a protobuf schema file using the name of the specified package.
	 * @param targetPackage the package whose name corresponds to the protobuf schema file
	 * @return
	 */
	private static String getResourceName(Package targetPackage) {
		return targetPackage.getName() + ".proto";
	}

	@Override
	public void registerSchema(SerializationContext context) {
		try {
			context.registerProtoFiles(getFileDescriptorSource(this.loader, this.resourceName));
		} catch (DescriptorParserException e) {
			try {
				// If parsing failed, unregister this schema so others can register
				context.unregisterProtoFile(this.resourceName);
			} catch (RuntimeException ignore) {
				// Ignore
			}
			throw e;
		}
	}

	@Override
	public String toString() {
		return this.resourceName;
	}

	@SuppressWarnings("removal")
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
			if (exception instanceof IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
			throw new RuntimeException(exception);
		}
	}
}
