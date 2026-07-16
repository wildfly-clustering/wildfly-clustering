/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Generic marshaller based on two non-public members.
 * @param <T> the target type of this marshaller
 * @param <M1> the first component member type
 * @param <M2> the second component member type
 * @author Paul Ferraro
 */
public class BinaryMemberMarshaller<T, M1, M2> extends AbstractMemberMarshaller<T> {

	private final Class<M1> member1Type;
	private final Class<M2> member2Type;
	private final BiFunction<M1, M2, T> factory;

	/**
	 * Creates a marshaller for the specified members.
	 * @param type the marshalled object type
	 * @param handleFactory the member handle factory
	 * @param member1Type the former member type
	 * @param member2Type the latter member type
	 * @param factory the marshalled object factory
	 */
	public BinaryMemberMarshaller(Class<T> type, BiFunction<Class<T>, Class<?>, Function<T, Object>> handleFactory, Class<M1> member1Type, Class<M2> member2Type, BiFunction<M1, M2, T> factory) {
		super(type, handleFactory, member1Type, member2Type);
		this.member1Type = member1Type;
		this.member2Type = member2Type;
		this.factory = factory;
	}

	@Override
	public T apply(Object[] parameters) {
		return this.factory.apply(this.member1Type.cast(parameters[0]), this.member2Type.cast(parameters[1]));
	}
}
