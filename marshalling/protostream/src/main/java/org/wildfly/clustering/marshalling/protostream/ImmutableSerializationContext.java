/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.ProtobufUtil;
import org.wildfly.clustering.function.Predicate;
import org.wildfly.clustering.marshalling.MarshallerConfigurationBuilder;
import org.wildfly.clustering.marshalling.protostream.lang.LangSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.lang.invoke.LangInvokeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.math.MathSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.net.NetSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.sql.SQLSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.time.TimeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.JavaUtilSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.UtilSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.ConcurrentSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.atomic.AtomicSerializationContextInitializer;

/**
 * An immutable serialization context extension.
 * @author Paul Ferraro
 */
public interface ImmutableSerializationContext extends org.infinispan.protostream.ImmutableSerializationContext {

	@Override
	ProtoStreamConfiguration getConfiguration();

	@Override
	<T> ProtoStreamMarshaller<T> getMarshaller(Class<T> targetClass);

	@Override
	<T> ProtoStreamMarshaller<T> getMarshaller(T object);

	@Override
	<T> ProtoStreamMarshaller<T> getMarshaller(String fullTypeName);

	/**
	 * Returns a stream of marshallable types.
	 * @return a stream of marshallable types.
	 */
	Stream<Class<?>> stream();

	/**
	 * Returns a reader context for the specified input.
	 * @param input an input stream
	 * @return a reader context for the specified input.
	 * @throws IOException if the stream could not be read
	 */
	default ProtobufTagMarshaller.ReadContext createReadContext(InputStream input) throws IOException {
		return this.createReadContext(input, input.available());
	}

	/**
	 * Returns a reader context for the specified input.
	 * @param input an input stream
	 * @param length the number of bytes to read from the specified input
	 * @return a reader context for the specified input.
	 * @throws IOException if the stream could not be read
	 */
	ProtobufTagMarshaller.ReadContext createReadContext(InputStream input, int length) throws IOException;

	/**
	 * Returns a writer context for the specified output.
	 * @param output an output stream
	 * @return a writer context for the specified output.
	 */
	ProtobufTagMarshaller.WriteContext createWriteContext(OutputStream output);

	/**
	 * Returns a size context.
	 * @return a size context.
	 */
	ProtoStreamMarshaller.SizeContext createSizeContext();

	/**
	 * A builder of a serialization context.
	 */
	interface Builder extends MarshallerConfigurationBuilder<ImmutableSerializationContext, SerializationContextInitializer, Builder> {
		/**
		 * Returns a serialization context builder using the specified configuration
		 * @param configuration a ProtoStream configuration
		 * @return a serialization context builder using the specified configuration
		 */
		static Builder with(ProtoStreamConfiguration configuration) {
			return new DefaultBuilder(configuration).load(configuration.getClassLoaderResolver().getDefaultClassLoader());
		}
	}

	/**
	 * Default serialization context builder.
	 */
	class DefaultBuilder implements Builder {
		private static final System.Logger LOGGER = System.getLogger(SerializationContextBuilder.class.getName());
		private static final String PROTOSTREAM_BASE_PACKAGE_NAME = org.infinispan.protostream.BaseMarshaller.class.getPackage().getName();

		private final SerializationContext context;

		DefaultBuilder(ProtoStreamConfiguration configuration) {
			this(new DefaultSerializationContext(configuration, ProtobufUtil.newSerializationContext(configuration)));
		}

		DefaultBuilder(SerializationContext context) {
			this.context = context;
			// Load default schemas first, so they can be referenced by loader-specific schemas
			this.register(new LangSerializationContextInitializer());
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
			this.register(new LangInvokeSerializationContextInitializer());
		}

		@Override
		public Builder register(SerializationContextInitializer initializer) {
			initializer.initialize(this.context);
			return this;
		}

		@Override
		public Builder load(ClassLoader loader) {
			this.loadWildFly(loader);
			this.loadNative(loader);
			return this;
		}

		private void loadWildFly(ClassLoader loader) {
			List<SerializationContextInitializer> loaded = Privileged.loadAll(SerializationContextInitializer.class, loader);
			if (!loaded.isEmpty()) {
				Queue<SerializationContextInitializer> unregistered = new ArrayDeque<>(loaded);
				Queue<SerializationContextInitializer> registered = new ArrayDeque<>(loaded.size());
				List<DescriptorParserException> exceptions = new ArrayList<>(loaded.size());
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
							throw exceptions.get(0);
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
			for (org.infinispan.protostream.SerializationContextInitializer initializer : Privileged.loadAll(org.infinispan.protostream.SerializationContextInitializer.class, loader)) {
				if (!initializer.getClass().getName().startsWith(PROTOSTREAM_BASE_PACKAGE_NAME)) {
					LOGGER.log(System.Logger.Level.DEBUG, "Registering native marshallers/schemas from {0}", initializer.getClass().getName());
					initializer.register(this.context);
				}
			}
		}

		@Override
		public ImmutableSerializationContext build() {
			// Verify that there are no message types without a corresponding marshaller
			this.context.stream().allMatch(Predicate.of(true));
			Deque<Integer> missingTypeIds = new LinkedList<>();
			for (int typeId = 0; typeId <= Integer.MAX_VALUE; ++typeId) {
				try {
					String name = this.context.getDescriptorByTypeId(typeId).getFullName();
					while (!missingTypeIds.isEmpty()) {
						logTypeId(missingTypeIds.removeFirst(), "NONE");
					}
					logTypeId(typeId, name);
				} catch (IllegalArgumentException e) {
					if (missingTypeIds.size() > Byte.MAX_VALUE) break;
					missingTypeIds.add(typeId);
				}
			}
			return this.context;
		}

		private static void logTypeId(int typeId, String name) {
			LOGGER.log(System.Logger.Level.DEBUG, "@TypeId({0}) = {1}", typeId, name);
		}
	}
}
