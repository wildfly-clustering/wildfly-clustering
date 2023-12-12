/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.marshalling.protostream;

import java.util.function.UnaryOperator;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.wildfly.clustering.cache.infinispan.marshalling.UserMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContextBuilder;

/**
 * @author Paul Ferraro
 */
public class ProtoStreamMarshaller extends UserMarshaller {
	public static final String MEDIA_TYPE_NAME = "application/x-protostream;agent=wildfly-clustering";
	public static final MediaType MEDIA_TYPE = MediaType.fromString(MEDIA_TYPE_NAME);

	public ProtoStreamMarshaller(ClassLoaderMarshaller loaderMarshaller, UnaryOperator<SerializationContextBuilder> builder) {
		this(builder.apply(SerializationContextBuilder.newInstance(loaderMarshaller).register(new IOSerializationContextInitializer())).build());
	}

	public ProtoStreamMarshaller(ImmutableSerializationContext context) {
		super(MEDIA_TYPE, new ProtoStreamByteBufferMarshaller(context));
	}
}
