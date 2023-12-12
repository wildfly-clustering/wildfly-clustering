/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import java.io.IOException;

import org.wildfly.clustering.marshalling.protostream.FunctionalScalarMarshaller;
import org.wildfly.clustering.marshalling.protostream.ScalarMarshaller;
import org.wildfly.common.function.ExceptionFunction;

/**
 * @author Paul Ferraro
 */
public class KeyScalarMarshaller<I, K extends Key<I>> extends FunctionalScalarMarshaller<K, I> {

	public KeyScalarMarshaller(Class<K> targetClass, ScalarMarshaller<I> marshaller, ExceptionFunction<I, K, IOException> resolver) {
		super(targetClass, marshaller, Key::getId, resolver);
	}
}
