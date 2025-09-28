/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jboss.marshalling.ChainingClassTable;
import org.jboss.marshalling.ChainingObjectTable;
import org.jboss.marshalling.ClassResolver;
import org.jboss.marshalling.ClassTable;
import org.jboss.marshalling.Externalizer;
import org.jboss.marshalling.MappingClassExternalizerFactory;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.ObjectTable;
import org.jboss.marshalling.SerializabilityChecker;
import org.wildfly.clustering.marshalling.MarshallerConfigurationBuilder;

/**
 * Builds a JBoss Marshalling configuration
 * @author Paul Ferraro
 */
public interface MarshallingConfigurationBuilder extends MarshallerConfigurationBuilder<MarshallingConfiguration, ExternalizerProvider, MarshallingConfigurationBuilder> {

	/**
	 * Constructs a builder of a {@link MarshallingConfiguration} using the specified class resolver.
	 * @param resolver a class resolver
	 * @return a new builder
	 */
	static MarshallingConfigurationBuilder newInstance(ClassResolver resolver) {
		return new DefaultMarshallingConfigurationBuilder(resolver);
	}

	/**
	 * Builder of a JBoss marshalling configuration.
	 */
	class DefaultMarshallingConfigurationBuilder implements MarshallingConfigurationBuilder {
		private final MarshallingConfiguration configuration = new MarshallingConfiguration();
		private final Map<Class<?>, Externalizer> externalizers = new LinkedHashMap<>();
		private final Stream.Builder<ClassTable> classTables = Stream.builder();
		private final Stream.Builder<ObjectTable> objectTables = Stream.builder();

		DefaultMarshallingConfigurationBuilder(ClassResolver resolver) {
			this.configuration.setClassResolver(resolver);
			this.configuration.setSerializabilityChecker(SerializabilityChecker.DEFAULT);

			EnumSet.allOf(DefaultClassTableProvider.class).stream().map(Supplier::get).forEach(this.classTables);
			EnumSet.allOf(DefaultObjectTableProvider.class).stream().map(Supplier::get).forEach(this.objectTables);
		}

		@Override
		public MarshallingConfigurationBuilder register(ExternalizerProvider provider) {
			this.externalizers.put(provider.getType(), provider.getExternalizer());
			return this;
		}

		@Override
		public MarshallingConfigurationBuilder load(ClassLoader loader) {
			loadAll(ExternalizerProvider.class, loader, this::register);
			loadAll(ClassTable.class, loader, this.classTables);
			loadAll(ObjectTable.class, loader, this.objectTables);
			return this;
		}

		@Override
		public MarshallingConfiguration build() {
			if (!this.externalizers.isEmpty()) {
				// Add class/object tables for externalizers
				this.classTables.add(new IdentityClassTable(new ArrayList<>(this.externalizers.keySet())));
				this.objectTables.add(new IdentityObjectTable(new ArrayList<>(this.externalizers.values())));
			}
			this.configuration.setClassExternalizerFactory(new MappingClassExternalizerFactory(this.externalizers));
			this.configuration.setClassTable(new ChainingClassTable(this.classTables.build().toArray(ClassTable[]::new)));
			this.configuration.setObjectTable(new ChainingObjectTable(this.objectTables.build().toArray(ObjectTable[]::new)));
			return this.configuration;
		}

		@SuppressWarnings("removal")
		static <T> void loadAll(Class<T> targetClass, ClassLoader loader, Consumer<T> consumer) {
			AccessController.doPrivileged(new PrivilegedAction<>() {
				@Override
				public Void run() {
					ServiceLoader.load(targetClass, loader).stream().map(Supplier::get).forEach(consumer);
					return null;
				}
			});
		}
	}
}
