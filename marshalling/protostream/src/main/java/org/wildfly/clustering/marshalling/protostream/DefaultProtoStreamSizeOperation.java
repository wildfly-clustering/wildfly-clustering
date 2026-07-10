/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.util.OptionalInt;

import org.wildfly.clustering.marshalling.protostream.AbstractProtoStreamWriter.DefaultProtoStreamWriterContext;
import org.wildfly.clustering.marshalling.protostream.AbstractProtoStreamWriter.ProtoStreamWriterContext;

/**
 * A default ProtoStream size operation.
 * @author Paul Ferraro
 */
public class DefaultProtoStreamSizeOperation extends AbstractProtoStreamOperation implements ProtoStreamSizeOperation {

	private final ProtoStreamWriterContext context;

	/**
	 * Creates a new ProtoStream size operation using a new context.
	 * @param context the serialization context
	 */
	DefaultProtoStreamSizeOperation(ProtoStreamTagMarshaller.SizeContext writer, ImmutableSerializationContext context) {
		this(writer, context, new DefaultProtoStreamWriterContext());
	}

	/**
	 * Creates a new ProtoStream size operation using the specified context.
	 * @param context the serialization context
	 * @param sizeContext the context of the size operation
	 */
	DefaultProtoStreamSizeOperation(ProtoStreamTagMarshaller.SizeContext writer, ImmutableSerializationContext context, ProtoStreamWriterContext sizeContext) {
		super(writer, context);
		this.context = sizeContext;
	}

	@Override
	public Context getContext() {
		return this.context;
	}

	@Override
	public <T> OptionalInt computeSize(Writable<T> operation, T value) {
		SizeComputingProtoStreamWriter writer = new SizeComputingProtoStreamWriter(this.getSerializationContext(), this.context);
		try {
			operation.writeTo(writer, value);
			return writer.get();
		} catch (IOException e) {
			return OptionalInt.empty();
		}
	}
}
