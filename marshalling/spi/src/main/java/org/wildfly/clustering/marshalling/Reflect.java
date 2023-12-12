/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
		return java.security.AccessController.doPrivileged(new PrivilegedAction<Field>() {
			@Override
			public Field run() {
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
		});
	}

	static Object getValue(Object source, Field field) {
		return getValue(source, field, Object.class);
	}

	static <T> T getValue(Object source, Field field, Class<T> fieldType) {
		return java.security.AccessController.doPrivileged(new PrivilegedAction<T>() {
			@Override
			public T run() {
				try {
					return fieldType.cast(field.get(source));
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}
}
