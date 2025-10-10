/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.OptionalInt;

import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufTagMarshaller.ReadContext;
import org.infinispan.protostream.ProtobufTagMarshaller.WriteContext;
import org.infinispan.protostream.impl.TagReaderImpl;
import org.infinispan.protostream.impl.TagWriterImpl;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;

/**
 * A ProtoStream byte buffer marshaller.
 * @author Paul Ferraro
 */
public class ProtoStreamByteBufferMarshaller implements ByteBufferMarshaller {

	private final ImmutableSerializationContext context;

	/**
	 * Constructs a new ProtoStream marshaller using the specified context
	 * @param context a serialization context
	 */
	public ProtoStreamByteBufferMarshaller(ImmutableSerializationContext context) {
		// N.B. Marshallers in WildFly require TCCL to resolve org.jboss.weld.Container
		this.context = context;
	}

	@Override
	public OptionalInt size(Object object) {
		ProtoStreamSizeOperation operation = new DefaultProtoStreamSizeOperation(this.context);
		ProtoStreamMarshaller<Any> marshaller = operation.findMarshaller(Any.class);
		return marshaller.size(operation, new Any(object));
	}

	@Override
	public boolean test(Object object) {
		if ((object == null) || (object instanceof Class)) return true;
		Class<?> targetClass = object.getClass();
		if (AnyField.fromJavaType(targetClass) != null) return true;
		if (targetClass.isArray()) {
			for (int i = 0; i < Array.getLength(object); ++i) {
				if (!this.test(Array.get(object, i))) return false;
			}
			return true;
		}
		if (Proxy.isProxyClass(targetClass)) {
			return this.test(Proxy.getInvocationHandler(object));
		}
		while (targetClass != null) {
			if (this.context.canMarshall(targetClass)) {
				return true;
			}
			targetClass = targetClass.getSuperclass();
		}
		return false;
	}

	@Override
	public Object readFrom(InputStream input) throws IOException {
		ReadContext context = TagReaderImpl.newInstance(this.context, input);
		ProtoStreamReader reader = new DefaultProtoStreamReader(context);
		ProtoStreamMarshaller<Any> marshaller = reader.findMarshaller(Any.class);
		return marshaller.readFrom(reader).get();
	}

	@Override
	public void writeTo(OutputStream output, Object object) throws IOException {
		WriteContext context = TagWriterImpl.newInstance(this.context, output);
		ProtoStreamWriter writer = new DefaultProtoStreamWriter(context);
		ProtoStreamMarshaller<Any> marshaller = writer.findMarshaller(Any.class);
		marshaller.writeTo(writer, new Any(object));
	}

	@Override
	public String toString() {
		return "ProtoStream";
	}
}
