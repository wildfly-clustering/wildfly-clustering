/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.config.Configuration;
import org.wildfly.clustering.marshalling.MarshallerConfigurationBuilder;

/**
 * A builder of a native Infinispan serialization context.
 * @author Paul Ferraro
 */
public interface SerializationContextBuilder extends MarshallerConfigurationBuilder<ImmutableSerializationContext, SerializationContextInitializer, SerializationContextBuilder> {

	/**
	 * Constructs a builder of a native {@link SerializationContext}.
	 * @param configuration a native ProtoStream configuration
	 * @return a new builder
	 */
	static SerializationContextBuilder with(Configuration configuration) {
		SerializationContext context = ProtobufUtil.newSerializationContext(configuration);
		return new SerializationContextBuilder() {

			@Override
			public SerializationContextBuilder register(SerializationContextInitializer initializer) {
				initializer.register(context);
				return this;
			}

			@Override
			public SerializationContextBuilder load(ClassLoader loader) {
				for (SerializationContextInitializer initializer : Privileged.loadAll(SerializationContextInitializer.class, loader)) {
					this.register(initializer);
				}
				return this;
			}

			@Override
			public ImmutableSerializationContext build() {
				return context;
			}
		};
	}
}
