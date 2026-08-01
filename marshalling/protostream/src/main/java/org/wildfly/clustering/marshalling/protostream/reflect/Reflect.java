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

import org.wildfly.clustering.function.BiFunction;
import org.wildfly.clustering.function.Function;

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

	private static MethodHandles.Lookup privateLookup(Class<?> reflected) throws IllegalAccessException {
		return MethodHandles.privateLookupIn(reflected, LOOKUP);
	}

	static <T, R> Function<T, R> findVarHandle(Class<? extends T> sourceClass, Class<? extends R> fieldType) {
		Field field = findField(sourceClass, fieldType);
		try {
			MethodHandle handle = privateLookup(field.getDeclaringClass()).findGetter(field.getDeclaringClass(), field.getName(), field.getType());
			return Function.invoke(handle);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			return value -> {
				throw new IllegalStateException(e);
			};
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
		return findMethodHandle(method.getDeclaringClass(), method.getName(), type);
	}

	static <T, R> Function<T, R> findMethodHandle(Class<?> sourceClass, String name, MethodType type) {
		try {
			MethodHandle handle = privateLookup(sourceClass).findVirtual(sourceClass, name, type);
			return Function.invoke(handle);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			return Function.throwing(e);
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

	static <T, R> Function<T, R> getConstructorHandle(Class<? extends R> sourceClass, Class<? super T> parameterType) {
		try {
			MethodHandle handle = privateLookup(sourceClass).findConstructor(sourceClass, MethodType.methodType(void.class, parameterType));
			return Function.invoke(handle);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			return value -> {
				throw new IllegalStateException(e);
			};
		}
	}

	static <T1, T2, R> BiFunction<T1, T2, R> getConstructorHandle(Class<? extends R> sourceClass, Class<? super T1> parameter1Type, Class<? super T2> parameter2Type) {
		try {
			MethodHandle handle = privateLookup(sourceClass).findConstructor(sourceClass, MethodType.methodType(void.class, parameter1Type, parameter2Type));
			return BiFunction.invoke(handle);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			return (value1, value2) -> {
				throw new IllegalStateException(e);
			};
		}
	}

	static <T1, T2, T3, R> TriFunction<T1, T2, T3, R> getConstructorHandle(Class<? extends R> sourceClass, Class<? super T1> parameter1Type, Class<? super T2> parameter2Type, Class<? super T3> parameter3Type) {
		try {
			MethodHandle handle = privateLookup(sourceClass).findConstructor(sourceClass, MethodType.methodType(void.class, parameter1Type, parameter2Type, parameter3Type));
			return TriFunction.invoke(handle);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			return TriFunction.throwing(e);
		}
	}
}
