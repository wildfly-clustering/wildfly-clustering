/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility methods requiring privileged actions for use by reflection-based marshallers.
 * Do not change class/method visibility to avoid being called from other {@link java.security.CodeSource}s, thus granting privilege escalation to external code.
 * @author Paul Ferraro
 */
final class Reflect {

	static Field findField(Class<?> sourceClass, Class<?> fieldType) {
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
			Field field = assignableFields.get(0);
			field.setAccessible(true);
			return field;
		}
		Class<?> superClass = sourceClass.getSuperclass();
		if ((superClass == null) || (superClass == Object.class)) {
			throw new IllegalArgumentException(fieldType.getName());
		}
		return findField(superClass, fieldType);
	}

	static Method findMethod(Class<?> sourceClass, Class<?> returnType) {
		List<Method> matchingMethods = new LinkedList<>();
		for (Method method : sourceClass.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers()) && (method.getParameterCount() == 0) && (method.getReturnType() == returnType)) {
				matchingMethods.add(method);
			}
		}
		// We should not have matched more than 1 method
		if (matchingMethods.size() > 1) {
			throw new IllegalStateException(matchingMethods.toString());
		}
		if (!matchingMethods.isEmpty()) {
			Method method = matchingMethods.get(0);
			method.setAccessible(true);
			return method;
		}
		Class<?> superClass = sourceClass.getSuperclass();
		if ((superClass == null) || (superClass == Object.class)) {
			throw new IllegalArgumentException(returnType.getName());
		}
		return findMethod(superClass, returnType);
	}

	static Method findMethod(Class<?> sourceClass, String methodName) {
		try {
			Method method = sourceClass.getDeclaredMethod(methodName);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	static <T> Constructor<T> getConstructor(Class<T> sourceClass, Class<?>... parameterTypes) {
		try {
			Constructor<T> constructor = sourceClass.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return constructor;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	static <T> T newInstance(Constructor<T> constructor, Object... parameters) {
		try {
			return constructor.newInstance(parameters);
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	static Object getValue(Object source, Field field) {
		return getValue(source, field, Object.class);
	}

	static <T> T getValue(Object source, Field field, Class<T> fieldType) {
		try {
			return fieldType.cast(field.get(source));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static void setValue(Object source, Field field, Object value) {
		try {
			field.set(source, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static Object invoke(Object source, Method method) {
		return invoke(source, method, Object.class);
	}

	static <T> T invoke(Object source, Method method, Class<T> returnClass) {
		try {
			return returnClass.cast(method.invoke(source));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}
}
