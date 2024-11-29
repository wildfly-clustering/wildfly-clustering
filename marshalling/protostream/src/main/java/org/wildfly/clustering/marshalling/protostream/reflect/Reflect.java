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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility methods requiring privileged actions for use by reflection-based marshallers.
 * Do not change class/method visibility to avoid being called from other {@link java.security.CodeSource}s, thus granting privilege escalation to external code.
 * @author Paul Ferraro
 */
final class Reflect {

	static Field findField(Class<?> sourceClass, Class<?> fieldType) {
		if (System.getSecurityManager() == null) {
			return findFieldUnchecked(sourceClass, fieldType);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Field run() {
				return findFieldUnchecked(sourceClass, fieldType);
			}
		});
	}

	private static Field findFieldUnchecked(Class<?> sourceClass, Class<?> fieldType) {
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
		return findFieldUnchecked(superClass, fieldType);
	}

	static Method findMethod(Class<?> sourceClass, Class<?> returnType) {
		if (System.getSecurityManager() == null) {
			return findMethodUnchecked(sourceClass, returnType);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Method run() {
				return findMethodUnchecked(sourceClass, returnType);
			}
		});
	}

	private static Method findMethodUnchecked(Class<?> sourceClass, Class<?> returnType) {
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
		return findMethodUnchecked(superClass, returnType);
	}

	static Method findMethod(Class<?> sourceClass, String methodName) {
		if (System.getSecurityManager() == null) {
			return findMethodUnchecked(sourceClass, methodName);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Method run() {
				return findMethodUnchecked(sourceClass, methodName);
			}
		});
	}

	private static Method findMethodUnchecked(Class<?> sourceClass, String methodName) {
		try {
			Method method = sourceClass.getDeclaredMethod(methodName);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	static <T> Constructor<T> getConstructor(Class<T> sourceClass, Class<?>... parameterTypes) {
		if (System.getSecurityManager() == null) {
			return getConstructorUnchecked(sourceClass, parameterTypes);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public Constructor<T> run() {
				return getConstructorUnchecked(sourceClass, parameterTypes);
			}
		});
	}

	private static <T> Constructor<T> getConstructorUnchecked(Class<T> sourceClass, Class<?>... parameterTypes) {
		try {
			Constructor<T> constructor = sourceClass.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return constructor;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	static <T> T newInstance(Constructor<T> constructor, Object... parameters) {
		if (System.getSecurityManager() == null) {
			return newInstanceUnchecked(constructor, parameters);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public T run() {
				return newInstanceUnchecked(constructor, parameters);
			}
		});
	}

	private static <T> T newInstanceUnchecked(Constructor<T> constructor, Object... parameters) {
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
		if (System.getSecurityManager() == null) {
			return getValueUnchecked(source, field, fieldType);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public T run() {
				return getValueUnchecked(source, field, fieldType);
			}
		});
	}

	private static <T> T getValueUnchecked(Object source, Field field, Class<T> fieldType) {
		try {
			return fieldType.cast(field.get(source));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	static void setValue(Object source, Field field, Object value) {
		if (System.getSecurityManager() == null) {
			setValueUnchecked(source, field, value);
		} else {
			AccessController.doPrivileged(new PrivilegedAction<>() {
				@Override
				public Void run() {
					setValueUnchecked(source, field, value);
					return null;
				}
			});
		}
	}

	private static void setValueUnchecked(Object source, Field field, Object value) {
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
		if (System.getSecurityManager() == null) {
			return invokeUnchecked(source, method, returnClass);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public T run() {
				return invokeUnchecked(source, method, returnClass);
			}
		});
	}

	private static <T> T invokeUnchecked(Object source, Method method, Class<T> returnClass) {
		try {
			return returnClass.cast(method.invoke(source));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}
}
