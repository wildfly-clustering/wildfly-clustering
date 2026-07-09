/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.lang.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.wildfly.clustering.function.Function;

/**
 * Methods requiring permission checking when a security manager is enabled.
 * @author Paul Ferraro
 */
class Privileged {
	private static final Package PRIVILEGED_PACKAGE = SerializedLambda.class.getPackage();

	private Privileged() {
		// Hide
	}

	@SuppressWarnings("removal")
	static MethodHandle getMethodHandle(Class<?> sourceClass, String methodName, MethodType type) {
		if (System.getSecurityManager() == null) {
			return getMethodHandleUnchecked(sourceClass, methodName, type);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public MethodHandle run() {
				return getMethodHandleUnchecked(sourceClass, methodName, type);
			}
		});
	}

	@SuppressWarnings("removal")
	static <T, R> Function<T, R> getFieldHandle(Class<T> sourceClass, Class<R> fieldType) {
		if (System.getSecurityManager() == null) {
			return getFieldHandleUnchecked(sourceClass, fieldType);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Function<T, R> run() {
				return getFieldHandleUnchecked(sourceClass, fieldType);
			}
		});
	}

	private static MethodHandle getMethodHandleUnchecked(Class<?> sourceClass, String methodName, MethodType type) {
		try {
			Method method = sourceClass.getDeclaredMethod(methodName, type.parameterArray());
			return MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).unreflect(method);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private static <T, R> Function<T, R> getFieldHandleUnchecked(Class<T> sourceClass, Class<R> fieldType) {
		for (Field field : sourceClass.getDeclaredFields()) {
			if (field.getType() == fieldType) {
				// MethodHandles.privateLookupIn(...) forbids use of java.lang.invoke package.
				if (sourceClass.getPackage() == PRIVILEGED_PACKAGE) {
					field.setAccessible(true);
					return new Function<>() {
						@Override
						public R apply(T source) {
							try {
								return fieldType.cast(field.get(source));
							} catch (IllegalAccessException e) {
								throw new IllegalStateException(e);
							}
						}
					};
				}
				try {
					MethodHandle handle = MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).unreflectGetter(field);
					return new Function<>() {
						@Override
						public R apply(T source) {
							try {
								return fieldType.cast(handle.invoke(source));
							} catch (Throwable e) {
								throw new IllegalStateException(e);
							}
						}
					};
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		throw new IllegalArgumentException(String.format("%s::%s", sourceClass.getCanonicalName(), fieldType.getCanonicalName()));
	}
}
