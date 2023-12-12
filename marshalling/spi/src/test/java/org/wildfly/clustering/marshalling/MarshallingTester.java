/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Validates correctness of the marshalling of an object.
 * 
 * @author Paul Ferraro
 */
public class MarshallingTester<T> implements Tester<T> {

	private final TestMarshaller<T> marshaller;
	private final List<ByteBufferMarshaller> benchmarkMarshallers;

	public MarshallingTester(TestMarshaller<T> marshaller, List<ByteBufferMarshaller> benchmarkMarshallers) {
		this.marshaller = marshaller;
		this.benchmarkMarshallers = benchmarkMarshallers;
	}

	@Override
	public void test(T subject, BiConsumer<T, T> assertion) throws IOException {
		ByteBuffer buffer = this.marshaller.write(subject);
		int size = buffer.limit() - buffer.arrayOffset();

		if (subject != null) {
			// Uncomment to report payload size
			// System.out.println(String.format("%s\t%s\t%s", (subject instanceof Enum) ? ((Enum<?>) subject).getDeclaringClass().getCanonicalName() : subject.getClass().getCanonicalName(), (subject instanceof Character) ? (int) (Character) subject : subject, size));
		}

		T result = this.marshaller.read(buffer);

		assertion.accept(subject, result);

		// Verify that we have improved upon benchmarks
		for (ByteBufferMarshaller benchmarkMarshaller : this.benchmarkMarshallers) {
			if (benchmarkMarshaller.isMarshallable(subject)) {
				ByteBuffer serializationBuffer = benchmarkMarshaller.write(subject);
				int serializationSize = serializationBuffer.limit() - serializationBuffer.arrayOffset();
				assertTrue(size < serializationSize, () -> String.format("Marshaller size = %d, %s serialization size = %d", size, benchmarkMarshaller.getClass().getSimpleName(), serializationSize));
			}
		}
	}
}
