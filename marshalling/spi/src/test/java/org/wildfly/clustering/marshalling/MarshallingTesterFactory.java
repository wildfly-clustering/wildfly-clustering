/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.util.List;

/**
 * Factory for creating marshalling testers.
 * @author Paul Ferraro
 */
public interface MarshallingTesterFactory {

	default <T> MarshallingTester<T> createTester() {
		return new MarshallingTester<>(new ByteBufferTestMarshaller<>(this.getMarshaller()), this.getBenchmarkMarshallers());
	}

	default <E extends Enum<E>> EnumMarshallingTester<E> createTester(Class<E> enumClass) {
		return new EnumMarshallingTester<>(enumClass, this.createTester());
	}

	ByteBufferMarshaller getMarshaller();

	default List<ByteBufferMarshaller> getBenchmarkMarshallers() {
		return List.of();
	}
}
