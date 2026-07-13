/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Methods requiring permission checking when a security manager is enabled.
 * @author Paul Ferraro
 */
@SuppressWarnings("removal")
class Privileged {
	private Privileged() {
		// Hide
	}

	static ClassLoader getClassLoader(Class<?> targetClass) {
		return getClassLoader(targetClass::getClassLoader);
	}

	static ClassLoader getClassLoader(Module module) {
		return getClassLoader(module::getClassLoader);
	}

	private static ClassLoader getClassLoader(Supplier<ClassLoader> provider) {
		if (System.getSecurityManager() == null) {
			return provider.get();
		}
		return java.security.AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public ClassLoader run() {
				return provider.get();
			}
		});
	}

	static <T> List<T> loadAll(Class<T> targetClass, ClassLoader loader) {
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public List<T> run() {
				return ServiceLoader.load(targetClass, loader).stream().map(Supplier::get).toList();
			}
		});
	}

	static Optional<MethodHandle> findMethodHandle(Class<?> sourceClass, String methodName, MethodType type) {
		if (System.getSecurityManager() == null) {
			return findMethodHandleUnchecked(sourceClass, methodName, type);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Optional<MethodHandle> run() {
				return findMethodHandleUnchecked(sourceClass, methodName, type);
			}
		});
	}

	private static Optional<MethodHandle> findMethodHandleUnchecked(Class<?> sourceClass, String methodName, MethodType type) {
		try {
			Method method = sourceClass.getDeclaredMethod(methodName, type.parameterArray());
			return Optional.of(MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).unreflect(method));
		} catch (NoSuchMethodException e) {
			return Optional.empty();
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
