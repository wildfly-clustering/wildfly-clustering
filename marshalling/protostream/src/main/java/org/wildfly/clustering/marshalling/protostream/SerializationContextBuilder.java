/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.config.Configuration;
import org.infinispan.protostream.impl.SerializationContextImpl;
import org.wildfly.clustering.marshalling.MarshallerConfigurationBuilder;
import org.wildfly.clustering.marshalling.protostream.math.MathSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.net.NetSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.sql.SQLSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.time.TimeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.JavaUtilSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.UtilSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.ConcurrentSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.atomic.AtomicSerializationContextInitializer;

/**
 * A builder of a serialization context.
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
		return newInstance(marshaller, DefaultSerializationContext::new);
	}

	/**
	 * Constructs a builder of a {@link SerializationContext} using a default set of initializers.
	 * @param marshaller the marshaller used to write/resolve a ClassLoader
	 * @param wrapper a serialization context wrapper
	 * @return a new builder
	 */
	static SerializationContextBuilder<SerializationContextInitializer> newInstance(ClassLoaderMarshaller marshaller, Function<org.infinispan.protostream.SerializationContext, SerializationContext> wrapper) {
		// Don't register WrappedMessage marshaller
		return new DefaultSerializationContextBuilder(wrapper.apply(new SerializationContextImpl(Configuration.builder().build())), marshaller);
	}

	/**
	 * Constructs a builder of a native {@link SerializationContext}.
	 * @return a new builder
	 */
	static SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> newInstance() {
		return newInstance(UnaryOperator.identity());
	}

	/**
	 * Constructs a builder of a native {@link SerializationContext}.
	 * @param wrapper a serialization context wrapper
	 * @return a new builder
	 */
	static SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> newInstance(UnaryOperator<org.infinispan.protostream.SerializationContext> wrapper) {
		return new NativeSerializationContextBuilder(wrapper.apply(ProtobufUtil.newSerializationContext(Configuration.builder().build())));
	}

	/**
	 * Default serialization context builder.
	 */
	class DefaultSerializationContextBuilder implements SerializationContextBuilder<SerializationContextInitializer> {
		private static final System.Logger LOGGER = System.getLogger(SerializationContextBuilder.class.getName());
		private static final String PROTOSTREAM_BASE_PACKAGE_NAME = org.infinispan.protostream.BaseMarshaller.class.getPackage().getName();

		private final SerializationContext context;

		DefaultSerializationContextBuilder(SerializationContext context, ClassLoaderMarshaller marshaller) {
			this.context = context;
			// Load default schemas first, so they can be referenced by loader-specific schemas
			this.register(new LangSerializationContextInitializer(marshaller));
			this.register(new AnySerializationContextInitializer());
			this.register(new MathSerializationContextInitializer());
			this.register(new NetSerializationContextInitializer());
			this.register(new TimeSerializationContextInitializer());
			this.register(new SQLSerializationContextInitializer());
			this.register(new UtilSerializationContextInitializer());
			this.register(new JavaUtilSerializationContextInitializer());
			this.register(new AtomicSerializationContextInitializer());
			this.register(new ConcurrentSerializationContextInitializer());
			this.register(new MarshallingSerializationContextInitializer());
		}

		@Override
		public SerializationContextBuilder<SerializationContextInitializer> register(SerializationContextInitializer initializer) {
			initializer.initialize(this.context);
			return this;
		}

		@Override
		public SerializationContextBuilder<SerializationContextInitializer> load(ClassLoader loader) {
			this.loadWildFly(loader);
			this.loadNative(loader);
			return this;
		}

		private void loadWildFly(ClassLoader loader) {
			List<SerializationContextInitializer> loaded = loadAll(SerializationContextInitializer.class, loader);
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
							LOGGER.log(System.Logger.Level.DEBUG, "Registering marshallers/schemas from {0}", initializer.getClass().getName());
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
			for (org.infinispan.protostream.SerializationContextInitializer initializer : loadAll(org.infinispan.protostream.SerializationContextInitializer.class, loader)) {
				if (!initializer.getClass().getName().startsWith(PROTOSTREAM_BASE_PACKAGE_NAME)) {
					LOGGER.log(System.Logger.Level.DEBUG, "Registering native marshallers/schemas from {0}", initializer.getClass().getName());
					initializer.register(this.context);
				}
			}
		}

		@Override
		public ImmutableSerializationContext build() {
			ImmutableSerializationContext context = this.context.getImmutableSerializationContext();
			Deque<Integer> missingTypeIds = new LinkedList<>();
			for (int typeId = 0; typeId <= Integer.MAX_VALUE; ++typeId) {
				try {
					String name = context.getDescriptorByTypeId(typeId).getFullName();
					while (!missingTypeIds.isEmpty()) {
						logTypeId(missingTypeIds.removeFirst(), "NONE");
					}
					logTypeId(typeId, name);
				} catch (IllegalArgumentException e) {
					if (missingTypeIds.size() > Byte.MAX_VALUE) break;
					missingTypeIds.add(typeId);
				}
			}
			return context;
		}

		private static void logTypeId(int typeId, String name) {
			LOGGER.log(System.Logger.Level.DEBUG, "@TypeId({0}) = {1}", typeId, name);
		}
	}

	/**
	 * Native serialization context builder.
	 */
	class NativeSerializationContextBuilder implements SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> {
		private final org.infinispan.protostream.SerializationContext context;

		NativeSerializationContextBuilder(org.infinispan.protostream.SerializationContext context) {
			this.context = context;
		}

		@Override
		public SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> register(org.infinispan.protostream.SerializationContextInitializer initializer) {
			initializer.register(this.context);
			return this;
		}

		@Override
		public SerializationContextBuilder<org.infinispan.protostream.SerializationContextInitializer> load(ClassLoader loader) {
			for (org.infinispan.protostream.SerializationContextInitializer initializer : loadAll(org.infinispan.protostream.SerializationContextInitializer.class, loader)) {
				this.register(initializer);
			}
			return this;
		}

		@Override
		public ImmutableSerializationContext build() {
			return this.context;
		}
	}

	@SuppressWarnings("removal")
	private static <T> List<T> loadAll(Class<T> targetClass, ClassLoader loader) {
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public List<T> run() {
				return ServiceLoader.load(targetClass, loader).stream().map(Supplier::get).toList();
			}
		});
	}
}
