/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.infinispan.protostream.DescriptorParserException;
import org.infinispan.protostream.ImmutableSerializationContext;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;

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

		private final DefaultSerializationContext context = new DefaultSerializationContext();

		DefaultSerializationContextBuilder(ClassLoaderMarshaller marshaller) {
			// Load default schemas first, so they can be referenced by loader-specific schemas
			this.register(new LangSerializationContextInitializer(marshaller));
			this.register(EnumSet.allOf(DefaultSerializationContextInitializerProvider.class));
		}

		@Override
		public SerializationContextBuilder register(SerializationContextInitializer initializer) {
			initializer.registerSchema(this.context);
			initializer.registerMarshallers(this.context);
			return this;
		}

		@Override
		public SerializationContextBuilder load(ClassLoader loader) {
			this.tryLoad(loader);
			return this;
		}

		@Override
		public SerializationContextBuilder require(ClassLoader loader) {
			if (!this.tryLoad(loader)) {
				throw new NoSuchElementException();
			}
			return this;
		}

		private boolean tryLoad(ClassLoader loader) {
			return this.tryRegister(Reflect.loadAll(SerializationContextInitializer.class, loader));
		}

		private boolean tryRegister(Collection<SerializationContextInitializer> initializers) {
			boolean registered = false;
			Collection<SerializationContextInitializer> retries = new LinkedList<>();
			int count = 0;
			DescriptorParserException exception = null;
			for (SerializationContextInitializer initializer : initializers) {
				// Do not load initializers from protostream-types
				if (!initializer.getClass().getName().startsWith(PROTOSTREAM_BASE_PACKAGE_NAME)) {
					count += 1;
					try {
						this.register(initializer);
						registered = true;
					} catch (DescriptorParserException e) {
						exception = e;
						// Descriptor might fail to parse due to dependency ordering issues
						retries.add(initializer);
					}
				}
			}
			if ((exception != null) && (retries.size() == count)) {
				throw exception;
			}
			// Retry any failed initializers
			return retries.isEmpty() ? registered : this.tryRegister(retries) || registered;
		}

		@Override
		public ImmutableSerializationContext build() {
			return this.context.get();
		}
	}
}
