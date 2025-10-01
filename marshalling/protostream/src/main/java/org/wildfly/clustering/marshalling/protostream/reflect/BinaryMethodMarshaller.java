/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * Generic marshaller based on two non-public accessor methods.
 * @param <T> the target type of this marshaller
 * @param <M1> the first component accessor method return type
 * @param <M2> the second component accessor method return type
 * @author Paul Ferraro
 */
public class BinaryMethodMarshaller<T, M1, M2> extends BinaryMemberMarshaller<T, Method, M1, M2> {

	/**
	 * Creates a marshaller for the specified methods.
	 * @param type the marshalled object type
	 * @param member1Type the former member type
	 * @param member2Type the latter member type
	 * @param factory the marshalled object factory
	 */
	public BinaryMethodMarshaller(Class<? extends T> type, Class<M1> member1Type, Class<M2> member2Type, BiFunction<M1, M2, T> factory) {
		super(type, Reflect::invoke, Reflect::findMethod, member1Type, member2Type, factory);
	}
}
