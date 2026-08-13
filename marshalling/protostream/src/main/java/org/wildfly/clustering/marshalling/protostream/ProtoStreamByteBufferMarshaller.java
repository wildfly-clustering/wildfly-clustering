/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.OptionalInt;
import java.util.function.Supplier;

import org.infinispan.protostream.ProtobufTagMarshaller.ReadContext;
import org.infinispan.protostream.ProtobufTagMarshaller.WriteContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.context.ThreadContextClassLoaderReference;
import org.wildfly.clustering.marshalling.AbstractByteBufferMarshaller;

/**
 * A ProtoStream byte buffer marshaller.
 * @author Paul Ferraro
 */
public class ProtoStreamByteBufferMarshaller extends AbstractByteBufferMarshaller {

	private final ImmutableSerializationContext context;
	private final ProtoStreamMarshaller<Any> marshaller;
	private final Supplier<Context<ClassLoader>> contextProvider;

	/**
	 * Constructs a new ProtoStream marshaller using the specified context
	 * @param context a serialization context
	 */
	public ProtoStreamByteBufferMarshaller(ImmutableSerializationContext context) {
		this(context, context.getConfiguration().getClassLoaderResolver().getDefaultClassLoader());
	}

	private ProtoStreamByteBufferMarshaller(ImmutableSerializationContext context, ClassLoader loader) {
		super(loader);
		this.context = context;
		this.marshaller = context.findMarshaller(Any.class);
		this.contextProvider = ThreadContextClassLoaderReference.CURRENT.provide(loader);
	}

	@Override
	public OptionalInt size(Object object) {
		try (Context<ClassLoader> loader = this.contextProvider.get()) {
			ProtoStreamMarshaller.SizeContext context = this.context.createSizeContext();
			ProtoStreamSizeOperation operation = new DefaultProtoStreamSizeOperation(context, this.context);
			return this.marshaller.size(operation, new Any(object));
		} catch (Throwable e) {
			return OptionalInt.empty();
		}
	}

	@Override
	public boolean test(Object object) {
		return this.marshaller.test(this.context, new Any(object));
	}

	@Override
	public Object readFrom(InputStream input) throws IOException {
		ReadContext context = this.context.createReadContext(input);
		ProtoStreamReader reader = new DefaultProtoStreamReader(context, this.context);
		return this.marshaller.readFrom(reader).get();
	}

	@Override
	public void writeTo(OutputStream output, Object object) throws IOException {
		WriteContext context = this.context.createWriteContext(output);
		ProtoStreamWriter writer = new DefaultProtoStreamWriter(context, this.context);
		this.marshaller.writeTo(writer, new Any(object));
	}

	@Override
	public String toString() {
		return "ProtoStream";
	}
}
