/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.wildfly.clustering.marshalling.protostream.math.MathSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.net.NetSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.sql.SQLSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.time.TimeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.UtilSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.ConcurrentSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.util.concurrent.atomic.AtomicSerializationContextInitializer;

/**
 * @author Paul Ferraro
 */
public interface SerializationContextBuilder {

	/**
	 * Constructs a builder of a {@link SerializationContext} using a default set of initializers.
	 */
	static SerializationContextBuilder newInstance(ClassLoaderMarshaller marshaller) {
		return new DefaultSerializationContextBuilder(marshaller);
	}

	/**
	 * Registers an initializer with the {@link org.infinispan.protostream.SerializationContext}.
	 * @param initializer an initializer for the {@link org.infinispan.protostream.SerializationContext}.
	 * @return a reference to this builder
	 */
	SerializationContextBuilder register(SerializationContextInitializer initializer);

	/**
	 * Bulk registers a number of initializers with the {@link org.infinispan.protostream.SerializationContext}.
	 * @param initializers a number of initializers of the {@link org.infinispan.protostream.SerializationContext}.
	 * @return a reference to this builder
	 */
	default SerializationContextBuilder register(Iterable<? extends SerializationContextInitializer> initializers) {
		for (SerializationContextInitializer initializer : initializers) {
			this.register(initializer);
		}
		return this;
	}

	/**
	 * Registers a number of initializer loaded from the specified class loader with the {@link org.infinispan.protostream.SerializationContext}.
	 * @param loader a class loader
	 * @return a reference to this builder
	 */
	SerializationContextBuilder load(ClassLoader loader);

	/**
	 * Registers a number of initializer loaded from the specified class loader with the {@link org.infinispan.protostream.SerializationContext}.
	 * @param loader a class loader
	 * @return a reference to this builder
	 * @throws NoSuchElementException if no initializers were loaded
	 */
	SerializationContextBuilder require(ClassLoader loader);

	/**
	 * Returns an immutable {@link org.infinispan.protostream.SerializationContext}.
	 * @return the completed and immutable serialization context
	 */
	ImmutableSerializationContext build();

	class DefaultSerializationContextBuilder implements SerializationContextBuilder {
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
		public SerializationContextBuilder register(SerializationContextInitializer initializer) {
			initializer.registerSchema(this.context);
			initializer.registerMarshallers(this.context);
			return this;
		}

		@Override
		public SerializationContextBuilder load(ClassLoader loader) {
			this.tryLoadAll(loader);
			return this;
		}

		@Override
		public SerializationContextBuilder require(ClassLoader loader) {
			if (!this.tryLoadAll(loader)) {
				throw new NoSuchElementException();
			}
			return this;
		}

		private boolean tryLoadAll(ClassLoader loader) {
			boolean loaded = this.tryLoad(loader);
			boolean loadedNative = this.tryLoadNative(loader);
			return loaded || loadedNative;
		}

		private boolean tryLoad(ClassLoader loader) {
			List<SerializationContextInitializer> loaded = Reflect.loadAll(SerializationContextInitializer.class, loader);
			if (loaded.isEmpty()) return false;

			List<SerializationContextInitializer> unregistered = new LinkedList<>(loaded);
			DescriptorParserException exception = null;
			while (!unregistered.isEmpty()) {
				int size = unregistered.size();
				Iterator<SerializationContextInitializer> remaining = unregistered.iterator();
				while (remaining.hasNext()) {
					SerializationContextInitializer initializer = remaining.next();
					try {
						initializer.registerSchema(this.context);
						initializer.registerMarshallers(this.context);
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
			return true;
		}

		private boolean tryLoadNative(ClassLoader loader) {
			boolean registered = false;
			for (org.infinispan.protostream.SerializationContextInitializer initializer : Reflect.loadAll(org.infinispan.protostream.SerializationContextInitializer.class, loader)) {
				if (!initializer.getClass().getName().startsWith(PROTOSTREAM_BASE_PACKAGE_NAME)) {
					initializer.registerSchema(this.context);
					initializer.registerMarshallers(this.context);
					registered = true;
				}
			}
			return registered;
		}

		@Override
		public ImmutableSerializationContext build() {
			return this.context.getImmutableSerializationContext();
		}
	}
}
