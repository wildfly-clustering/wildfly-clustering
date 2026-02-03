/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

/**
 * Generic marshaller based on a single non-public accessor method.
 * @param <T> the marshaller target type
 * @param <R> the method accessor result type
 * @author Paul Ferraro
 */
public class UnaryMethodMarshaller<T, R> extends UnaryMemberMarshaller<T, MethodHandle, R> {

	/**
	 * Creates a marshaller for the specified methods.
	 * @param targetClass the marshalled object type
	 * @param memberClass the member type
	 * @param factory the marshalled object factory
	 */
	public UnaryMethodMarshaller(Class<? extends T> targetClass, Class<R> memberClass, Function<R, T> factory) {
		super(targetClass, AbstractMemberMarshaller::invoke, Reflect::findMethodHandle, memberClass, factory);
	}
}
