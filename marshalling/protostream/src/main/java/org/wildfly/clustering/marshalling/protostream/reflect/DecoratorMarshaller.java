/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream.reflect;

import java.util.function.UnaryOperator;

/**
 * Marshaller for a decorator that does not provide public access to its decorated object.
 * @param <T> the target type of this marshaller
 * @author Paul Ferraro
 */
public class DecoratorMarshaller<T> extends UnaryFieldMarshaller<T, T> {

	/**
	 * Creates a marshaller for the specified decorator class.
	 * @param decoratedClass the marshalled object type
	 * @param decorator the decorator function
	 * @param sample a sample object
	 */
	public DecoratorMarshaller(Class<T> decoratedClass, UnaryOperator<T> decorator, T sample) {
		this(decorator.apply(sample).getClass().asSubclass(decoratedClass), decoratedClass, decorator);
	}

	private DecoratorMarshaller(Class<? extends T> decoratorClass, Class<T> decoratedClass, UnaryOperator<T> decorator) {
		super(decoratorClass, decoratedClass, decorator);
	}
}
