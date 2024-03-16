/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.UncheckedIOException;

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
		this.loader = this.getClass().getClassLoader();
	}

	protected AbstractSerializationContextInitializer(String resourceName) {
		this.resourceName = resourceName;
		this.loader = this.getClass().getClassLoader();
	}

	protected AbstractSerializationContextInitializer(String resourceName, Class<?> containingClass) {
		this.resourceName = resourceName;
		this.loader = containingClass.getClassLoader();
	}

	@Override
	public void registerSchema(SerializationContext context) {
		try {
			context.registerProtoFiles(FileDescriptorSource.fromResources(this.loader, this.resourceName));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
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
}
