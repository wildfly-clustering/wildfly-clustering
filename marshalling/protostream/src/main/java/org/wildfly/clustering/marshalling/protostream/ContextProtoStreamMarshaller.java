/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.util.OptionalInt;
import java.util.function.Supplier;

import org.wildfly.clustering.context.Context;

/**
 * Marshaller decorator that applies a context to the thread during marshalling operations.
 * @author Paul Ferraro
 * @param <T> the marshalled object type
 * @param <C> the context type
 */
public class ContextProtoStreamMarshaller<T, C> implements ProtoStreamMarshaller<T> {
	private final ProtoStreamMarshaller<T> marshaller;
	private final Supplier<Context<C>> contextProvider;

	ContextProtoStreamMarshaller(ProtoStreamMarshaller<T> marshaller, Supplier<Context<C>> contextProvider) {
		this.marshaller = marshaller;
		this.contextProvider = contextProvider;
	}

	@Override
	public Class<? extends T> getJavaClass() {
		return this.marshaller.getJavaClass();
	}

	@Override
	public String getTypeName() {
		return this.marshaller.getTypeName();
	}

	@Override
	public T readFrom(ProtoStreamReader reader) throws IOException {
		try (Context<C> context = this.contextProvider.get()) {
			return this.marshaller.readFrom(reader);
		}
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, T value) throws IOException {
		try (Context<C> context = this.contextProvider.get()) {
			this.marshaller.writeTo(writer, value);
		}
	}

	@Override
	public OptionalInt size(ProtoStreamSizeOperation operation, T value) {
		try (Context<C> context = this.contextProvider.get()) {
			return this.marshaller.size(operation, value);
		}
	}
}
