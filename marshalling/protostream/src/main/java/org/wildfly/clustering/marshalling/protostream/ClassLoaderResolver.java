/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.wildfly.clustering.function.Supplier;

/**
 * Encapsulates a class resolution strategy.
 * @author Paul Ferraro
 */
public interface ClassLoaderResolver {
	/**
	 * Returns the name from which a class loader can be resolved.
	 * @param value the class
	 * @return the name from which a class loader can be resolved.
	 */
	String classLoaderName(Class<?> value);

	/**
	 * Returns the class loader associated with the specified name.
	 * @param name a name from which to resolve a class loader.
	 * @return the class loader associated with the specified name.
	 */
	ClassLoader resolve(String name);

	/**
	 * Returns the default class loader of this resolver.
	 * @return the default class loader of this resolver.
	 */
	ClassLoader getDefaultClassLoader();

	/**
	 * Returns a resolver that always resolves classes from the specified {@link ClassLoader}.
	 * @param loader the loader from which classes will be resolved
	 * @return a resolver that always resolves classes from the specified {@link ClassLoader}.
	 */
	static ClassLoaderResolver of(ClassLoader loader) {
		return new ClassLoaderResolver() {
			@Override
			public String classLoaderName(Class<?> value) {
				return null;
			}

			@Override
			public ClassLoader resolve(String name) {
				return loader;
			}

			@Override
			public ClassLoader getDefaultClassLoader() {
				return loader;
			}
		};
	}

	/**
	 * Returns a resolver that resolves classes from the loader of a {@link Module}.
	 * @param defaultModule the module of the default class loader of this resolver
	 * @return a resolver that resolves classes from the loader of a {@link Module}.
	 */
	static ClassLoaderResolver of(Module defaultModule) {
		return of(defaultModule.getLayer(), defaultModule);
	}

	/**
	 * Returns a resolver that resolves classes from the loader of a {@link Module} from the specified {@link ModuleLayer}.
	 * @param layer the layer from which to locate modules
	 * @param defaultModule the module of the default class loader of this resolver
	 * @return a resolver that resolves classes from the loader of a {@link Module} from the specified {@link ModuleLayer}.
	 */
	static ClassLoaderResolver of(ModuleLayer layer, Module defaultModule) {
		return (layer != null) ? new ClassLoaderResolver() {
			@Override
			public String classLoaderName(Class<?> value) {
				return value.getModule().getName();
			}

			@Override
			public ClassLoader resolve(String name) {
				return layer.findModule(name).map(Module::getClassLoader).orElseThrow(Supplier.of(name).thenApply(IllegalArgumentException::new));
			}

			@Override
			public ClassLoader getDefaultClassLoader() {
				return defaultModule.getClassLoader();
			}
		} : of(defaultModule.getClassLoader());
	}
}
