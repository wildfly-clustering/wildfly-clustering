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
	public Object read(ByteBuffer buffer) throws IOException {
		try (Context context = this.contextProvider.get()) {
			return ByteBufferMarshaller.super.read(buffer);
		}
	}

	@Override
	public ByteBuffer write(Object object) throws IOException {
		try (Context context = this.contextProvider.get()) {
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
		try (Context context = this.contextProvider.get()) {
			this.writeTo(output, object);
			return OptionalInt.of(size.getPlain());
		} catch (Throwable e) {
			return OptionalInt.empty();
		}
	}}
