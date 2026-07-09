/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.function.Function;

import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.impl.TagWriterImpl;
import org.wildfly.clustering.marshalling.ByteBufferOutputStream;

/**
 * {@link ProtoStreamWriter} implementation that writes to a {@link TagWriterImpl}.
 * @author Paul Ferraro
 */
public class DefaultProtoStreamWriter extends AbstractProtoStreamWriter implements Function<Object, OptionalInt> {

	/**
	 * Creates a default ProtoStream writer.
	 * @param writeContext the write context
	 * @param context the serialization context
	 */
	public DefaultProtoStreamWriter(ProtobufTagMarshaller.WriteContext writeContext, ImmutableSerializationContext context) {
		this(writeContext, context, new DefaultProtoStreamWriterContext());
	}

	private DefaultProtoStreamWriter(ProtobufTagMarshaller.WriteContext writeContext, ImmutableSerializationContext context, ProtoStreamWriterContext writerContext) {
		super(writeContext, context, writerContext);
	}

	@Override
	public void writeObjectNoTag(Object value) throws IOException {
		ProtoStreamMarshaller<Object> marshaller = this.findMarshaller(value.getClass());
		OptionalInt size = this.getContext().computeSize(value, this);
		if (size.isPresent()) {
			// If size is known, we can marshal directly to our output stream
			int length = size.getAsInt();
			this.writeVarint32(length);
			if (length > 0) {
				marshaller.writeTo(this, value);
			}
		} else {
			// If size is unknown, marshal to an expandable temporary buffer
			// This should only be the case if delegating to JBoss Marshalling or Java Serialization
			try (ByteBufferOutputStream output = new ByteBufferOutputStream()) {
				ProtobufTagMarshaller.WriteContext writer = this.getSerializationContext().createWriteContext(output);
				marshaller.writeTo(new DefaultProtoStreamWriter(writer, this.getSerializationContext(), this.getContext()), value);
				// Byte buffer is array backed
				ByteBuffer buffer = output.getBuffer();
				int offset = buffer.arrayOffset() + buffer.position();
				int length = buffer.remaining();
				this.writeVarint32(length);
				if (length > 0) {
					this.writeRawBytes(buffer.array(), offset, length);
				}
			}
		}
	}

	@Override
	public OptionalInt apply(Object value) {
		ProtoStreamMarshaller<Object> marshaller = this.findMarshaller(value.getClass());
		// Retain reference integrity by using a copy of the current context during size operation
		return marshaller.size(new DefaultProtoStreamSizeOperation(this.getSerializationContext().createSizeContext(), this.getSerializationContext(), this.getContext().clone()), value);
	}
}
