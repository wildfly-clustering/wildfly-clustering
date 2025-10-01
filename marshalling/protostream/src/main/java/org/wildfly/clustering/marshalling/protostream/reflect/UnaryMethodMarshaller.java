/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Generic marshaller based on a single non-public accessor method.
 * @param <T> the marshaller target type
 * @param <M> the method accessor result type
 * @author Paul Ferraro
 */
public class UnaryMethodMarshaller<T, M> extends UnaryMemberMarshaller<T, Method, M> {

	/**
	 * Creates a marshaller for the specified methods.
	 * @param targetClass the marshalled object type
	 * @param memberClass the member type
	 * @param factory the marshalled object factory
	 */
	public UnaryMethodMarshaller(Class<? extends T> targetClass, Class<M> memberClass, Function<M, T> factory) {
		super(targetClass, Reflect::invoke, Reflect::findMethod, memberClass, factory);
	}
}
