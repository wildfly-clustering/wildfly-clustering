/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return targetClass.getClassLoader();
			}
		});
	}

	static <T> List<T> loadAll(Class<T> targetClass, ClassLoader loader) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public List<T> run() {
				return ServiceLoader.load(targetClass, loader).stream().map(Supplier::get).collect(Collectors.toList());
			}
		});
	}

	static <T> Optional<T> loadFirst(Class<T> targetClass, ClassLoader loader) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Optional<T> run() {
				return ServiceLoader.load(targetClass, loader).findFirst();
			}
		});
	}

	static FileDescriptorSource loadSchemas(String resourceName, ClassLoader loader) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
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

	static Method findMethod(Class<?> sourceClass, String methodName) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<Method>() {
			@Override
			public Method run() {
				try {
					Method method = sourceClass.getDeclaredMethod(methodName);
					method.setAccessible(true);
					return method;
				} catch (NoSuchMethodException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}

	static Object invoke(Object source, Method method) {
		return invoke(source, method, Object.class);
	}

	static <T> T invoke(Object source, Method method, Class<T> returnClass) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<T>() {
			@Override
			public T run() {
				try {
					return returnClass.cast(method.invoke(source));
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}
}
