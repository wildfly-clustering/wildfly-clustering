/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.infinispan.protostream.FileDescriptorSource;

/**
 * @author Paul Ferraro
 */
class Reflect {

	static ClassLoader getClassLoader(Class<?> targetClass) {
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return targetClass.getClassLoader();
			}
		});
	}

	static <T> List<T> loadAll(Class<T> targetClass, ClassLoader loader) {
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public List<T> run() {
				return ServiceLoader.load(targetClass, loader).stream().map(Supplier::get).collect(Collectors.toList());
			}
		});
	}

	static <T> Optional<T> loadFirst(Class<T> targetClass, ClassLoader loader) {
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Optional<T> run() {
				return ServiceLoader.load(targetClass, loader).findFirst();
			}
		});
	}

	static FileDescriptorSource loadSchemas(String resourceName, ClassLoader loader) {
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public FileDescriptorSource run() {
				try {
					return FileDescriptorSource.fromResources(loader, resourceName);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		});
	}
}
