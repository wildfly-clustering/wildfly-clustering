/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

/**
 * Generic marshaller based on two non-public fields.
 * @param <T> the target type of this marshaller
 * @param <F1> the first component field type
 * @param <F2> the second component field type
 * @author Paul Ferraro
 */
public class BinaryFieldMarshaller<T, F1, F2> extends BinaryMemberMarshaller<T, Field, F1, F2> {

	/**
	 * Creates a marshaller for the specified fields.
	 * @param type the marshalled object type
	 * @param field1Type the former field type
	 * @param field2Type the latter field type
	 * @param factory the object factory
	 */
	public BinaryFieldMarshaller(Class<? extends T> type, Class<F1> field1Type, Class<F2> field2Type, BiFunction<F1, F2, T> factory) {
		super(type, Reflect::getValue, Reflect::findField, field1Type, field2Type, factory);
	}
}
