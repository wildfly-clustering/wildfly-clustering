/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.UncheckedIOException;

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
		this.loader = this.getClass().getClassLoader();
	}

	/**
	 * Creates a new serialization context initializer that loads a protobuf schema file using the name of the specified package.
	 * @param targetPackage the package whose name corresponds to the protobuf schema file
	 */
	protected AbstractSerializationContextInitializer(Package targetPackage) {
		this.resourceName = getResourceName(targetPackage);
		this.loader = this.getClass().getClassLoader();
	}

	private static String getResourceName(Package targetPackage) {
		return targetPackage.getName() + ".proto";
	}

	@Override
	public void registerSchema(SerializationContext context) {
		context.registerProtoFiles(getFileDescriptorSource(this.loader, this.resourceName));
	}

	@Override
	public String toString() {
		return this.resourceName;
	}

	private static FileDescriptorSource getFileDescriptorSource(ClassLoader loader, String resourceName) {
		try {
			return FileDescriptorSource.fromResources(loader, resourceName);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
