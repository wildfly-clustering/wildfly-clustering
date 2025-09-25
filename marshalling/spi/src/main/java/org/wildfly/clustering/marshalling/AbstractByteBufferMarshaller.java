/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.context.ThreadContextClassLoaderReference;

/**
 * An abstract byte buffer marshaller that performs read/writing within a specified ClassLoader context.
 * @author Paul Ferraro
 */
public abstract class AbstractByteBufferMarshaller implements ByteBufferMarshaller {

	private final Supplier<Context<ClassLoader>> contextProvider;

	protected AbstractByteBufferMarshaller(ClassLoader loader) {
		this.contextProvider = ThreadContextClassLoaderReference.CURRENT.provide(loader);
	}

	@Override
	public Object read(ByteBuffer buffer) throws IOException {
		try (Context<ClassLoader> context = this.contextProvider.get()) {
			return ByteBufferMarshaller.super.read(buffer);
		}
	}

	@Override
	public ByteBuffer write(Object object) throws IOException {
		try (Context<ClassLoader> context = this.contextProvider.get()) {
			return ByteBufferMarshaller.super.write(object);
		}
	}
}
