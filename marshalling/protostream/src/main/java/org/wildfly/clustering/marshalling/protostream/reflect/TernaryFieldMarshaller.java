/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.invoke.VarHandle;

/**
 * Generic marshaller based on three non-public fields.
 * @param <T> the target type of this marshaller
 * @param <F1> the first component field type
 * @param <F2> the second component field type
 * @param <F3> the third component field type
 * @author Paul Ferraro
 */
public class TernaryFieldMarshaller<T, F1, F2, F3> extends TernaryMemberMarshaller<T, VarHandle, F1, F2, F3> {

	/**
	 * Creates a marshaller for the specified fields.
	 * @param type the marshalled object type
	 * @param field1Type the former field type
	 * @param field2Type the latter field type
	 * @param field3Type the tertiary field type
	 * @param factory the object factory
	 */
	public TernaryFieldMarshaller(Class<? extends T> type, Class<F1> field1Type, Class<F2> field2Type, Class<F3> field3Type, TriFunction<F1, F2, F3, T> factory) {
		super(type, AbstractMemberMarshaller::read, Reflect::findVarHandle, field1Type, field2Type, field3Type, factory);
	}
}
