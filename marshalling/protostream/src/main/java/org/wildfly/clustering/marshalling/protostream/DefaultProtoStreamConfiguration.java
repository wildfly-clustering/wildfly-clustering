/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.ObjectInputFilter;
import java.util.Optional;

/**
 * Default ProtoStream configuration.
 * @author Paul Ferraro
 */
class DefaultProtoStreamConfiguration implements ProtoStreamConfiguration {
	static final int REPEATED_FIELD_CAPACITY = Short.SIZE;
	static final int REPEATED_FIELD_CHUNK_SIZE = REPEATED_FIELD_CAPACITY * REPEATED_FIELD_CAPACITY;

	// <grumble> ConfigurationImpl is final, so we need to delegate rather than extend
	private final org.infinispan.protostream.config.Configuration configuration;
	private final Optional<ObjectInputFilter> filter;
	private final ClassLoaderResolver resolver;

	DefaultProtoStreamConfiguration(DefaultBuilder builder) {
		this.configuration = builder.builder.build();
		this.filter = builder.filter;
		this.resolver = builder.resolver;
	}

	@Override
	public ClassLoaderResolver getClassLoaderResolver() {
		return this.resolver;
	}

	@Override
	public Optional<ObjectInputFilter> getObjectInputFilter() {
		return this.filter;
	}

	@Override
	public boolean logOutOfSequenceReads() {
		return this.configuration.logOutOfSequenceReads();
	}

	@Override
	public boolean logOutOfSequenceWrites() {
		return this.configuration.logOutOfSequenceWrites();
	}

	@Override
	public int maxNestedMessageDepth() {
		return this.configuration.maxNestedMessageDepth();
	}

	@Override
	public SchemaValidation schemaValidation() {
		return this.configuration.schemaValidation();
	}

	@Override
	public AnnotationsConfig annotationsConfig() {
		return this.configuration.annotationsConfig();
	}

	// <grumble> ConfigurationImpl.BuilderImpl is final, so we need to delegate rather than extend
	static class DefaultBuilder implements Builder {
		private final org.infinispan.protostream.config.Configuration.Builder builder = org.infinispan.protostream.config.Configuration.builder();
		private final ClassLoaderResolver resolver;
		private Optional<ObjectInputFilter> filter = Optional.ofNullable(ObjectInputFilter.Config.getSerialFilter());

		DefaultBuilder(ClassLoaderResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public org.infinispan.protostream.config.Configuration.AnnotationsConfig.Builder annotationsConfig() {
			return this.builder.annotationsConfig();
		}

		@Override
		public Builder withObjectInputFilter(ObjectInputFilter filter) {
			this.filter = Optional.ofNullable(filter);
			return this;
		}

		@Override
		public Builder setLogOutOfSequenceReads(boolean logOutOfSequenceReads) {
			this.builder.setLogOutOfSequenceReads(logOutOfSequenceReads);
			return this;
		}

		@Override
		public Builder setLogOutOfSequenceWrites(boolean logOutOfSequenceWrites) {
			this.builder.setLogOutOfSequenceWrites(logOutOfSequenceWrites);
			return this;
		}

		@Override
		public Builder setLenient(boolean lenient) {
			this.builder.setLenient(lenient);
			return this;
		}

		@Override
		public Builder maxNestedMessageDepth(int maxNestedMessageDepth) {
			this.builder.maxNestedMessageDepth(maxNestedMessageDepth);
			return this;
		}

		@Override
		public Builder schemaValidation(SchemaValidation schemaValidation) {
			this.builder.schemaValidation(schemaValidation);
			return this;
		}

		@Override
		public ProtoStreamConfiguration build() {
			return new DefaultProtoStreamConfiguration(this);
		}
	}
}
