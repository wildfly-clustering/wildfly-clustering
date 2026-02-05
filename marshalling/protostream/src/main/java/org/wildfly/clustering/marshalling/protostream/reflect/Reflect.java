/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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
	private Reflect() {
		// Hide
	}

	@SuppressWarnings("removal")
	static VarHandle findVarHandle(Class<?> sourceClass, Class<?> fieldType) {
		if (System.getSecurityManager() == null) {
			return findVarHandleUnchecked(sourceClass, fieldType);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public VarHandle run() {
				return findVarHandleUnchecked(sourceClass, fieldType);
			}
		});
	}

	private static VarHandle findVarHandleUnchecked(Class<?> sourceClass, Class<?> fieldType) {
		try {
			return MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).unreflectVarHandle(findField(sourceClass, fieldType));
		} catch (IllegalAccessException e) {
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

	static MethodHandle findMethodHandle(Class<?> sourceClass, Class<?> returnType) {
		return findMethodHandle(sourceClass, MethodType.methodType(returnType));
	}

	@SuppressWarnings("removal")
	static MethodHandle findMethodHandle(Class<?> sourceClass, MethodType type) {
		if (System.getSecurityManager() == null) {
			return findMethodHandleUnchecked(sourceClass, type);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public MethodHandle run() {
				return findMethodHandleUnchecked(sourceClass, type);
			}
		});
	}

	private static MethodHandle findMethodHandleUnchecked(Class<?> sourceClass, MethodType type) {
		try {
			return MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).unreflect(findMethod(sourceClass, type));
		} catch (IllegalAccessException e) {
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

	private static MethodHandle getMethodHandleUnchecked(Class<?> sourceClass, String methodName, MethodType type) {
		try {
			return MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).findVirtual(sourceClass, methodName, type);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	static MethodHandle getConstructorHandle(Class<?> sourceClass, Class<?>... parameterTypes) {
		return getConstructorHandle(sourceClass, MethodType.methodType(void.class, parameterTypes));
	}

	@SuppressWarnings("removal")
	static MethodHandle getConstructorHandle(Class<?> sourceClass, MethodType type) {
		if (System.getSecurityManager() == null) {
			return getConstructorHandleUnchecked(sourceClass, type);
		}
		return AccessController.doPrivileged(new PrivilegedAction<>() {
			@Override
			public MethodHandle run() {
				return getConstructorHandleUnchecked(sourceClass, type);
			}
		});
	}

	private static MethodHandle getConstructorHandleUnchecked(Class<?> sourceClass, MethodType type) {
		try {
			return MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).findConstructor(sourceClass, type);
		} catch (IllegalAccessException | NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}
}
