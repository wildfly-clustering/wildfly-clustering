/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * Default ProtoStream configuration.
 * @author Paul Ferraro
 */
public class DefaultProtoStreamConfiguration implements ProtoStreamConfiguration {
	// <grumble> ConfigurationImpl is final, so we need to delegate rather than extend
	private final org.infinispan.protostream.config.Configuration configuration;
	private final ClassLoader loader;

	DefaultProtoStreamConfiguration(DefaultBuilder builder) {
		this.configuration = builder.builder.build();
		this.loader = builder.loader;
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.loader;
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

	@Override
	public boolean wrapCollectionElements() {
		return this.configuration.wrapCollectionElements();
	}

	// <grumble> ConfigurationImpl.BuilderImpl is final, so we need to delegate rather than extend
	static class DefaultBuilder implements Builder {
		private final org.infinispan.protostream.config.Configuration.Builder builder = org.infinispan.protostream.config.Configuration.builder();
		private final ClassLoader loader;

		DefaultBuilder(ClassLoader loader) {
			this.loader = loader;
		}

		@Override
		public org.infinispan.protostream.config.Configuration.AnnotationsConfig.Builder annotationsConfig() {
			return this.builder.annotationsConfig();
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
		public Builder wrapCollectionElements(boolean wrapCollectionElements) {
			this.builder.wrapCollectionElements(wrapCollectionElements);
			return this;
		}

		@Override
		public ProtoStreamConfiguration build() {
			return new DefaultProtoStreamConfiguration(this);
		}
	}
}
