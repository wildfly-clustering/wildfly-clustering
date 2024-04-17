/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.OptionalInt;

import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufUtil;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;

/**
 * A {@link ByteBufferMarshaller} based on a ProtoStream {@link org.infinispan.protostream.WrappedMessage}.
 * @author Paul Ferraro
 */
public class WrappedMessageByteBufferMarshaller implements ByteBufferMarshaller {

	private final ImmutableSerializationContext context;

	public WrappedMessageByteBufferMarshaller(ImmutableSerializationContext context) {
		this.context = context;
	}

	@Override
	public boolean isMarshallable(Object object) {
		return this.context.canMarshall(object);
	}

	@Override
	public Object readFrom(InputStream input) throws IOException {
		return ProtobufUtil.fromWrappedStream(this.context, input);
	}

	@Override
	public void writeTo(OutputStream output, Object object) throws IOException {
		ProtobufUtil.toWrappedStream(this.context, output, object);
	}

	@Override
	public OptionalInt size(Object object) {
		try {
			return OptionalInt.of(ProtobufUtil.computeWrappedMessageSize(this.context, object));
		} catch (IOException e) {
			return OptionalInt.empty();
		}
	}
}
