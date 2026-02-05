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
import java.util.function.Consumer;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.FileDescriptorSource;

/**
 * Registers a protobuf schema.
 * @author Paul Ferraro
 */
public class SchemaRegistrar implements Consumer<SerializationContext> {

	private final String resourceName;
	private final ClassLoader loader;

	/**
	 * Creates a new protobuf schema registrar for the specified initializer implementation class.
	 * @param initializerClass the serialization context initializer class
	 */
	public SchemaRegistrar(Class<? extends SerializationContextInitializer> initializerClass) {
		this(initializerClass, initializerClass.getPackage());
	}

	/**
	 * Creates a new protobuf schema registrar for the specified initializer implementation class and schema package.
	 * @param initializerClass the serialization context initializer class
	 * @param schemaPackage the package for which the protobuf schema file is named
	 */
	public SchemaRegistrar(Class<? extends SerializationContextInitializer> initializerClass, Package schemaPackage) {
		this.resourceName = schemaPackage.getName() + ".proto";
		this.loader = Privileged.getClassLoader(this.getClass());
	}

	@Override
	public void accept(SerializationContext context) {
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
