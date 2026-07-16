/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Generic marshaller based on a single non-public member.
 * @param <T> the marshaller target type
 * @param <M1> the component member type
 * @author Paul Ferraro
 */
public class UnaryMemberMarshaller<T, M1> extends AbstractMemberMarshaller<T> {

	private final Class<M1> memberType;
	private final Function<M1, T> factory;

	/**
	 * Creates a marshaller for the specified members.
	 * @param type the marshalled object type
	 * @param handleFactory the member handle factory
	 * @param memberType the member type
	 * @param factory the marshalled object factory
	 */
	public UnaryMemberMarshaller(Class<T> type, BiFunction<Class<T>, Class<?>, Function<T, Object>> handleFactory, Class<M1> memberType, Function<M1, T> factory) {
		super(type, handleFactory, memberType);
		this.memberType = memberType;
		this.factory = factory;
	}

	@Override
	public T apply(Object[] parameters) {
		return this.factory.apply(this.memberType.cast(parameters[0]));
	}
}
