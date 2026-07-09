/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

/**
 * A field of a marshaller.
 * @author Paul Ferraro
 * @param <T> the type of the associated marshaller
 */
public interface Field<T> {
	/**
	 * Returns the index of this field.
	 * @return the index of this field.
	 */
	int getIndex();

	/**
	 * Returns the marshaller for this field.
	 * @return the marshaller for this field.
	 */
	FieldMarshaller<T> getMarshaller();

	/**
	 * Returns a field for the specified class, or null, if not field exists.
	 * @param <T> the field type
	 * @param fieldClass the field type
	 * @return a field for the specified class, or null, if not field exists.
	 */
	@SuppressWarnings("unchecked")
	static <T> Field<T> forClass(Class<T> fieldClass) {
		return (Field<T>) AnyField.fromJavaType(fieldClass);
	}
}
