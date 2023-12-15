/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.infinispan.protostream.DescriptorParserException;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractSerializationContextInitializer implements SerializationContextInitializer {

	private final String resourceName;
	private final ClassLoader loader;

	protected AbstractSerializationContextInitializer() {
		this.resourceName = this.getClass().getPackage().getName() + ".proto";
		this.loader = Reflect.getClassLoader(this.getClass());
	}

	protected AbstractSerializationContextInitializer(String resourceName) {
		this.resourceName = resourceName;
		this.loader = Reflect.getClassLoader(this.getClass());
	}

	protected AbstractSerializationContextInitializer(String resourceName, Class<?> containingClass) {
		this.resourceName = resourceName;
		this.loader = Reflect.getClassLoader(containingClass);
	}

	@Override
	public void registerSchema(SerializationContext context) {
		try {
			context.registerProtoFiles(Reflect.loadSchemas(this.resourceName, this.loader));
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
