/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.ObjectInputFilter;
import java.util.Optional;

import org.infinispan.protostream.config.Configuration;

/**
 * ProtoStream configuration extension.
 * @author Paul Ferraro
 */
public interface ProtoStreamConfiguration extends Configuration {

	/**
	 * Returns the filter to apply to resolved classes.
	 * @return the filter to apply to resolved classes.
	 */
	Optional<ObjectInputFilter> getObjectInputFilter();

	/**
	 * Returns the marshaller of a class loader.
	 * @return the marshaller of a class loader.
	 */
	ClassLoaderResolver getClassLoaderResolver();

	@Deprecated
	@Override
	default boolean wrapCollectionElements() {
		return false;
	}

	/**
	 * Builder of a ProtoStream configuration.
	 */
	interface Builder extends org.infinispan.protostream.config.Configuration.Builder {
		/**
		 * Returns a new configuration builder instance.
		 * @param resolver the class loader resolver used by ProtoStream
		 * @return a new configuration builder instance.
		 */
		static Builder with(ClassLoaderResolver resolver) {
			return new DefaultProtoStreamConfiguration.DefaultBuilder(resolver);
		}

		/**
		 * Overrides the default input filter.
		 * @param filter an input filter.
		 * @return a reference to this builder
		 */
		Builder withObjectInputFilter(ObjectInputFilter filter);

		@Override
		Builder setLogOutOfSequenceReads(boolean logOutOfSequenceReads);

		@Override
		Builder setLogOutOfSequenceWrites(boolean logOutOfSequenceWrites);

		@Override
		Builder setLenient(boolean lenient);

		@Override
		Builder maxNestedMessageDepth(int maxNestedMessageDepth);

		@Override
		Builder schemaValidation(SchemaValidation schemaValidation);

		@Deprecated
		@Override
		default Builder wrapCollectionElements(boolean wrapCollectionElements) {
			throw new UnsupportedOperationException();
		}

		@Override
		ProtoStreamConfiguration build();
	}
}
