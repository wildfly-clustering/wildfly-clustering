/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Paul Ferraro
 */
final class Reflect {
	private Reflect() {
		// Hide
	}

	static VarHandle getVarHandle(Class<?> sourceClass, Class<?> fieldType) {
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
			try {
				return MethodHandles.privateLookupIn(sourceClass, MethodHandles.lookup()).unreflectVarHandle(field);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		Class<?> superClass = sourceClass.getSuperclass();
		if ((superClass == null) || (superClass == Object.class)) {
			throw new IllegalArgumentException(fieldType.getName());
		}
		return getVarHandle(superClass, fieldType);
	}

	static <T> T invoke(MethodHandle handle, Object source, Class<T> fieldType) {
		try {
			return fieldType.cast(handle.invokeExact(source));
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}
}
