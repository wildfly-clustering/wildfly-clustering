/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.lang.invoke.MethodHandle;

/**
 * Generic marshaller based on three non-public accessor methods.
 * @param <T> the target type of this marshaller
 * @param <R1> the first component accessor method return type
 * @param <R2> the second component accessor method return type
 * @param <R3> the third component accessor method return type
 * @author Paul Ferraro
 */
public class TernaryMethodMarshaller<T, R1, R2, R3> extends TernaryMemberMarshaller<T, MethodHandle, R1, R2, R3> {

	/**
	 * Creates a marshaller for the specified methods.
	 * @param type the marshalled object type
	 * @param member1Type the former member type
	 * @param member2Type the latter member type
	 * @param member3Type the tertiary member type
	 * @param factory the marshalled object factory
	 */
	public TernaryMethodMarshaller(Class<? extends T> type, Class<R1> member1Type, Class<R2> member2Type, Class<R3> member3Type, TriFunction<R1, R2, R3, T> factory) {
		super(type, AbstractMemberMarshaller::invoke, Reflect::findMethodHandle, member1Type, member2Type, member3Type, factory);
	}
}
