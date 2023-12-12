/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractSerializationContextInitializer implements SerializationContextInitializer {

	private final String resourceName;
	private final ClassLoader loader;

	protected AbstractSerializationContextInitializer() {
		this(null);
	}

	protected AbstractSerializationContextInitializer(String resourceName) {
		this(resourceName, null);
	}

	protected AbstractSerializationContextInitializer(String resourceName, ClassLoader loader) {
		this.resourceName = (resourceName == null) ? this.getClass().getPackage().getName() + ".proto" : resourceName;
		this.loader = (loader == null) ? Reflect.getClassLoader(this.getClass()) : loader;
	}

	@Deprecated
	@Override
	public final String getProtoFileName() {
		return null;
	}

	@Deprecated
	@Override
	public final String getProtoFile() {
		return null;
	}

	@Override
	public void registerSchema(SerializationContext context) {
		context.registerProtoFiles(Reflect.loadSchemas(this.resourceName, this.loader));
	}

	@Override
	public String toString() {
		return this.resourceName;
	}
}
