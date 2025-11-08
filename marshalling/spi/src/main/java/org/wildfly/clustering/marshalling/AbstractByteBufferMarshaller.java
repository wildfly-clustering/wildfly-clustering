/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.context.ThreadContextClassLoaderReference;

/**
 * An abstract byte buffer marshaller that performs read/writing within a specified ClassLoader context.
 * @author Paul Ferraro
 */
public abstract class AbstractByteBufferMarshaller implements ByteBufferMarshaller {

	private final Supplier<Context<ClassLoader>> contextProvider;

	/**
	 * Constructs a new byte buffer marshaller using the specified context class loader.
	 * @param loader a context class loader
	 */
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

	@Override
	public OptionalInt size(Object object) {
		AtomicInteger size = new AtomicInteger();
		OutputStream output = new OutputStream() {
			@Override
			public void write(int b) {
				size.setPlain(size.getPlain() + 1);
			}

			@Override
			public void write(byte[] bytes, int offset, int length) {
				size.setPlain(size.getPlain() + length);
			}
		};
		try {
			this.writeTo(output, object);
			return OptionalInt.of(size.getPlain());
		} catch (IOException e) {
			return OptionalInt.empty();
		}
	}
}
