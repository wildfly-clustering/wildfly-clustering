/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufUtil;
import org.wildfly.clustering.marshalling.MarshallerConfigurationBuilder;
import org.wildfly.clustering.marshalling.protostream.math.MathSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.net.NetSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.sql.SQLSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.time.TimeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.UtilSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.ConcurrentSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.atomic.AtomicSerializationContextInitializer;

/**
 * @author Paul Ferraro
 * @param <I> the initializer type
 */
public interface SerializationContextBuilder<I> extends MarshallerConfigurationBuilder<ImmutableSerializationContext, I, SerializationContextBuilder<I>> {

	/**
	 * Constructs a builder of a {@link SerializationContext} using a default set of initializers.
	 * @param marshaller the marshaller used to write/resolve a ClassLoader
	 * @return a new builder
	 */
	static SerializationContextBuilder<SerializationContextInitializer> newInstance(ClassLoaderMarshaller marshaller) {
		return new DefaultSerializationContextBuilder(marshaller);
	}

	/**
	 * Constructs a builder of a native {@link SerializationContext}.
	 * @return a new builder
	 */
	static SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> newInstance() {
		return new NativeSerializationContextBuilder();
	}

	class DefaultSerializationContextBuilder implements SerializationContextBuilder<SerializationContextInitializer> {
		private static final String PROTOSTREAM_BASE_PACKAGE_NAME = org.infinispan.protostream.BaseMarshaller.class.getPackage().getName();

		private final SerializationContext context = new DefaultSerializationContext();

		DefaultSerializationContextBuilder(ClassLoaderMarshaller marshaller) {
			// Load default schemas first, so they can be referenced by loader-specific schemas
			this.register(new LangSerializationContextInitializer(marshaller));
			this.register(new AnySerializationContextInitializer());
			this.register(new MathSerializationContextInitializer());
			this.register(new NetSerializationContextInitializer());
			this.register(new TimeSerializationContextInitializer());
			this.register(new SQLSerializationContextInitializer());
			this.register(new UtilSerializationContextInitializer());
			this.register(new AtomicSerializationContextInitializer());
			this.register(new ConcurrentSerializationContextInitializer());
			this.register(new MarshallingSerializationContextInitializer());
		}

		@Override
		public SerializationContextBuilder<SerializationContextInitializer> register(SerializationContextInitializer initializer) {
			initializer.registerSchema(this.context);
			initializer.registerMarshallers(this.context);
			return this;
		}

		@Override
		public SerializationContextBuilder<SerializationContextInitializer> load(ClassLoader loader) {
			this.loadWildFly(loader);
			this.loadNative(loader);
			return this;
		}

		private void loadWildFly(ClassLoader loader) {
			List<SerializationContextInitializer> loaded = Reflect.loadAll(SerializationContextInitializer.class, loader);
			if (!loaded.isEmpty()) {
				List<SerializationContextInitializer> unregistered = new LinkedList<>(loaded);
				DescriptorParserException exception = null;
				while (!unregistered.isEmpty()) {
					int size = unregistered.size();
					Iterator<SerializationContextInitializer> remaining = unregistered.iterator();
					while (remaining.hasNext()) {
						SerializationContextInitializer initializer = remaining.next();
						try {
							this.register(initializer);
							remaining.remove();
						} catch (DescriptorParserException e) {
							// Descriptor might fail to parse due to ordering issues
							// If so, retry this next iteration
							exception = e;
						}
					}
					// If we have made no progress give up
					if ((exception != null) && unregistered.size() == size) {
						throw exception;
					}
				}
			}
		}

		private void loadNative(ClassLoader loader) {
			for (org.infinispan.protostream.SerializationContextInitializer initializer : Reflect.loadAll(org.infinispan.protostream.SerializationContextInitializer.class, loader)) {
				if (!initializer.getClass().getName().startsWith(PROTOSTREAM_BASE_PACKAGE_NAME)) {
					initializer.registerSchema(this.context);
					initializer.registerMarshallers(this.context);
				}
			}
		}

		@Override
		public ImmutableSerializationContext build() {
			return this.context.getImmutableSerializationContext();
		}
	}

	class NativeSerializationContextBuilder implements SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> {
		private final org.infinispan.protostream.SerializationContext context = ProtobufUtil.newSerializationContext();

		@Override
		public SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> register(org.infinispan.protostream.SerializationContextInitializer initializer) {
			initializer.registerSchema(this.context);
			initializer.registerMarshallers(this.context);
			return this;
		}

		@Override
		public SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> load(ClassLoader loader) {
			for (org.infinispan.protostream.SerializationContextInitializer initializer : Reflect.loadAll(org.infinispan.protostream.SerializationContextInitializer.class, loader)) {
				this.register(initializer);
			}
			return this;
		}

		@Override
		public ImmutableSerializationContext build() {
			return this.context;
		}
	}
}
