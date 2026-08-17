/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.config.Configuration;
import org.infinispan.protostream.impl.SerializationContextImpl;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.context.ThreadContextClassLoaderReference;
import org.wildfly.clustering.function.BiFunction;
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
	static SerializationContextBuilder<SerializationContextInitializer> newInstance(ClassLoaderMarshaller marshaller, BiFunction<org.infinispan.protostream.SerializationContext, UnaryOperator<ProtoStreamMarshaller<?>>, SerializationContext> wrapper) {
		// Don't register WrappedMessage marshaller
		Supplier<Context<ClassLoader>> contextProvider = ThreadContextClassLoaderReference.CURRENT.provide(marshaller.createInitialValue());
		UnaryOperator<ProtoStreamMarshaller<?>> decorator = new UnaryOperator<>() {
			@Override
			public ProtoStreamMarshaller<?> apply(ProtoStreamMarshaller<?> marshaller) {
				return marshaller.getJavaClass().isEnum() ? marshaller : new ContextProtoStreamMarshaller<>(marshaller, contextProvider);
			}
		};
		return new DefaultSerializationContextBuilder(wrapper.apply(new SerializationContextImpl(Configuration.builder().build()), decorator), marshaller);
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
			List<SerializationContextInitializer> loaded = ServiceLoader.load(SerializationContextInitializer.class, loader).stream().map(Supplier::get).toList();
			if (!loaded.isEmpty()) {
				Queue<SerializationContextInitializer> unregistered = new ArrayDeque<>(loaded);
				Queue<SerializationContextInitializer> registered = new ArrayDeque<>(loaded.size());
				Queue<DescriptorParserException> exceptions = new ArrayDeque<>(loaded.size());
				// Register schemas first: determine initialization order before registering marshallers.
				while (!unregistered.isEmpty()) {
					SerializationContextInitializer initializer = unregistered.remove();
					Set<String> startFiles = this.context.getFileDescriptors().keySet();
					try {
						LOGGER.log(System.Logger.Level.TRACE, "Registering schemas from {0}", initializer.getClass().getName());
						initializer.registerSchema(this.context);
						LOGGER.log(System.Logger.Level.TRACE, "Registered schemas from {0}", initializer.getClass().getName());
						registered.add(initializer);
						exceptions.clear();
					} catch (DescriptorParserException e) {
						// Schema registration can fail due to ordering issues
						// Unregister any successfully registered schemas so that we can retry later
						Set<String> files = new HashSet<>(this.context.getFileDescriptors().keySet());
						files.removeAll(startFiles);
						if (!files.isEmpty()) {
							this.context.unregisterProtoFiles(files);
						}
						exceptions.add(e);
						// Add to tail of queue
						unregistered.add(initializer);
						// Give up if all initializers failed
						if (exceptions.size() == unregistered.size()) {
							throw exceptions.element();
						}
						LOGGER.log(System.Logger.Level.TRACE, "Deferring schema registration from {0} due to: {1}", initializer.getClass().getName(), e.getLocalizedMessage());
					}
				}
				// Register marshallers in the order the schemas were registered
				for (SerializationContextInitializer initializer : registered) {
					LOGGER.log(System.Logger.Level.DEBUG, "Registering marshallers from {0}", initializer.getClass().getName());
					initializer.registerMarshallers(this.context);
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
