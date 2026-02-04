/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.invoke.MethodHandle;
import java.util.function.BiFunction;

/**
 * Generic marshaller based on two non-public accessor methods.
 * @param <T> the target type of this marshaller
 * @param <R1> the first component accessor method return type
 * @param <R2> the second component accessor method return type
 * @author Paul Ferraro
 */
public class BinaryMethodMarshaller<T, R1, R2> extends BinaryMemberMarshaller<T, MethodHandle, R1, R2> {

	/**
	 * Creates a marshaller for the specified methods.
	 * @param type the marshalled object type
	 * @param member1Type the former member type
	 * @param member2Type the latter member type
	 * @param factory the marshalled object factory
	 */
	public BinaryMethodMarshaller(Class<? extends T> type, Class<R1> member1Type, Class<R2> member2Type, BiFunction<R1, R2, T> factory) {
		super(type, AbstractMemberMarshaller::invoke, Reflect::findMethodHandle, member1Type, member2Type, factory);
	}
}
