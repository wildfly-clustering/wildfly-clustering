/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.function.Supplier;

import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.context.ContextClassLoaderReference;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractByteBufferMarshaller implements ByteBufferMarshaller {

	private final Supplier<Context> contextProvider;

	protected AbstractByteBufferMarshaller(ClassLoader loader) {
		this.contextProvider = ContextClassLoaderReference.INSTANCE.provide(loader);
	}

	@Override
	public Supplier<Context> getContextProvider() {
		return this.contextProvider;
	}
}
