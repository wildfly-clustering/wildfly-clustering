/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.ProtobufTagMarshaller;
import org.infinispan.protostream.ProtobufUtil;
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
			for (org.infinispan.protostream.SerializationContextInitializer initializer : Privileged.loadAll(org.infinispan.protostream.SerializationContextInitializer.class, loader)) {
				if (!initializer.getClass().getName().startsWith(PROTOSTREAM_BASE_PACKAGE_NAME)) {
					LOGGER.log(System.Logger.Level.DEBUG, "Registering native marshallers/schemas from {0}", initializer.getClass().getName());
					initializer.register(this.context);
				}
			}
		}

		@Override
		public ImmutableSerializationContext build() {
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
