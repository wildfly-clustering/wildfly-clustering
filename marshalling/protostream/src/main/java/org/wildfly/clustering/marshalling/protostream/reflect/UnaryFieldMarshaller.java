/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

/**
 * Generic marshaller based on a single non-public field.
 * @param <T> the marshaller target type
 * @param <F> the field type
 * @author Paul Ferraro
 */
public class UnaryFieldMarshaller<T, F> extends UnaryMemberMarshaller<T, VarHandle, F> {

	/**
	 * Creates a marshaller for the specified field.
	 * @param targetClass the marshalled object type
	 * @param fieldClass the field type
	 * @param factory the object factory
	 */
	public UnaryFieldMarshaller(Class<? extends T> targetClass, Class<F> fieldClass, Function<F, T> factory) {
		super(targetClass, AbstractMemberMarshaller::read, Reflect::findVarHandle, fieldClass, factory);
	}

	/**
	 * Creates a marshaller for the specified field.
	 * @param targetClass the marshalled object type
	 * @param fieldClass the field type
	 */
	public UnaryFieldMarshaller(Class<? extends T> targetClass, Class<F> fieldClass) {
		this(targetClass, fieldClass, Reflect.getConstructorHandle(targetClass, fieldClass));
	}

	private UnaryFieldMarshaller(Class<? extends T> targetClass, Class<F> fieldClass, MethodHandle constructor) {
		this(targetClass, fieldClass, value -> invoke(constructor, value));
	}
}
