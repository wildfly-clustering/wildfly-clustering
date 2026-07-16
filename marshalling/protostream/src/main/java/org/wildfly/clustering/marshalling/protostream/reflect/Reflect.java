/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Utility methods requiring privileged actions for use by reflection-based marshallers.
 * Do not change class/method visibility to avoid being called from other {@link java.security.CodeSource}s, thus granting privilege escalation to external code.
 * @author Paul Ferraro
 */
final class Reflect {
	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

	private Reflect() {
		// Hide
	}

	private static MethodHandles.Lookup privateLookup(Class<?> reflected) {
		try {
			return MethodHandles.privateLookupIn(reflected, LOOKUP);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static <T, R> Function<T, R> findVarHandle(Class<? extends T> sourceClass, Class<? extends R> fieldType) {
		Field field = findField(sourceClass, fieldType);
		try {
			MethodHandle handle = privateLookup(sourceClass).findGetter(field.getDeclaringClass(), field.getName(), field.getType());
			return new Function<>() {
				@Override
				public R apply(T object) {
					try {
						return fieldType.cast(handle.invoke(object));
					} catch (Throwable e) {
						if (e instanceof RuntimeException exception) {
							throw exception;
						}
						if (e instanceof RuntimeException error) {
							throw error;
						}
						throw new IllegalStateException(e);
					}
				}
			};
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Field findField(Class<?> sourceClass, Class<?> fieldType) {
		List<Field> assignableFields = new LinkedList<>();
		Field[] fields = sourceClass.getDeclaredFields();
		// Try first with precise type checking
		for (Field field : fields) {
			Class<?> type = field.getType();
			if (!Modifier.isStatic(field.getModifiers()) && (type == fieldType)) {
				assignableFields.add(field);
			}
		}
		// Retry with relaxed type checking, if necessary
		if (assignableFields.isEmpty()) {
			for (Field field : fields) {
				Class<?> type = field.getType();
				if (!Modifier.isStatic(field.getModifiers()) && (type != Object.class) && type.isAssignableFrom(fieldType)) {
					assignableFields.add(field);
				}
			}
		}
		// We should not have matched more than 1 field
		if (assignableFields.size() > 1) {
			throw new IllegalStateException(assignableFields.toString());
		}
		if (!assignableFields.isEmpty()) {
			return assignableFields.get(0);
		}
		Class<?> superClass = sourceClass.getSuperclass();
		if ((superClass == null) || (superClass == Object.class)) {
			throw new IllegalArgumentException(fieldType.getName());
		}
		return findField(superClass, fieldType);
	}

	static <T, R> Function<T, R> findMethodHandle(Class<? extends T> sourceClass, Class<? extends R> returnType) {
		MethodType type = MethodType.methodType(returnType);
		Method method = findMethod(sourceClass, type);
		return findMethodHandle(sourceClass, method.getName(), type);
	}

	static <T, R> Function<T, R> findMethodHandle(Class<? extends T> sourceClass, String name, MethodType type) {
		try {
			MethodHandle handle = MethodHandles.lookup().findVirtual(sourceClass, name, type);
			return new Function<>() {
				@Override
				public R apply(T value) {
					try {
						return (R) handle.invoke(value);
					} catch (Throwable e) {
						if (e instanceof RuntimeException exception) {
							throw exception;
						}
						if (e instanceof RuntimeException error) {
							throw error;
						}
						throw new IllegalStateException(e);
					}
				}
			};
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Method findMethod(Class<?> sourceClass, MethodType type) {
		List<Method> matchingMethods = new LinkedList<>();
		for (Method method : sourceClass.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers()) && (method.getParameterCount() == type.parameterCount()) && (method.getReturnType() == type.returnType())) {
				Parameter[] parameter = method.getParameters();
				boolean found = true;
				for (int i = 0; i < method.getParameterCount(); ++i) {
					if (parameter[i].getType() != type.parameterType(i)) {
						found = false;
					}
				}
				if (found) {
					matchingMethods.add(method);
				}
			}
		}
		// We should not have matched more than 1 method
		if (matchingMethods.size() > 1) {
			throw new IllegalStateException(matchingMethods.toString());
		}
		if (!matchingMethods.isEmpty()) {
			return matchingMethods.get(0);
		}
		Class<?> superClass = sourceClass.getSuperclass();
		if ((superClass == null) || (superClass == Object.class)) {
			throw new IllegalArgumentException(type.returnType().getName());
		}
		return findMethod(superClass, type);
	}

	static MethodHandle getConstructorHandle(Class<?> sourceClass, Class<?>... parameterTypes) {
		return getConstructorHandle(sourceClass, MethodType.methodType(void.class, parameterTypes));
	}

	static MethodHandle getConstructorHandle(Class<?> sourceClass, MethodType type) {
		try {
			return MethodHandles.lookup().findConstructor(sourceClass, type);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}
}
