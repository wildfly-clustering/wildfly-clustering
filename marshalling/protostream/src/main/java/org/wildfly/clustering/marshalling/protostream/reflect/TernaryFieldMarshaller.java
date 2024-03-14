/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.reflect.Field;

/**
 * Generic marshaller based on three non-public fields.
 * @param <T> the target type of this marshaller
 * @param <F1> the first component field type
 * @param <F2> the second component field type
 * @param <F3> the third component field type
 * @author Paul Ferraro
 */
public class TernaryFieldMarshaller<T, F1, F2, F3> extends TernaryMemberMarshaller<T, Field, F1, F2, F3> {

	public TernaryFieldMarshaller(Class<? extends T> type, Class<F1> field1Type, Class<F2> field2Type, Class<F3> field3Type, TriFunction<F1, F2, F3, T> factory) {
		super(type, Reflect::getValue, Reflect::findField, field1Type, field2Type, field3Type, factory);
	}
}
