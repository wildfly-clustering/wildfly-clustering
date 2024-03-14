/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;

/**
 * Factory for creating marshalling testers.
 * @author Paul Ferraro
 */
public interface MarshallingTesterFactory extends TesterFactory {

	default <T> Tester<T> createTester(BiConsumer<T, T> assertion) {
		ByteBufferMarshaller marshaller = this.getMarshaller();
		return new Tester<>() {
			@Override
			public void accept(T subject) {
				try {
					assertTrue(marshaller.isMarshallable(subject), () -> Optional.ofNullable(subject).map(Object::toString).orElse(null));

					OptionalInt size = marshaller.size(subject);
					ByteBuffer buffer = marshaller.write(subject);

					int bufferSize = buffer.limit() - buffer.arrayOffset();

					if (size.isPresent()) {
						assertEquals(size.getAsInt(), bufferSize);
					}

					if (subject != null) {
						Class<?> subjectClass = (subject instanceof Enum) ? ((Enum<?>) subject).getDeclaringClass() : subject.getClass();
						Object subjectValue = (subject instanceof Character) ? (int) (Character) subject : subject;
						System.out.println(String.format("%s\t%s\t%s\t%d", marshaller, subjectClass.getCanonicalName(), subjectValue, bufferSize));
					}

					@SuppressWarnings("unchecked")
					T result = (T) marshaller.read(buffer);

					assertion.accept(subject, result);
				} catch (IOException e) {
					fail(e);
				}
			}

			@Override
			public void reject(T subject) {
				assertFalse(marshaller.isMarshallable(subject), subject::toString);
			}

			@Override
			public <E extends Throwable> void reject(T subject, Class<E> expected) {
				assertTrue(marshaller.isMarshallable(subject), subject::toString);
				try {
					ByteBuffer buffer = marshaller.write(subject);
					// If we were able to marshal, expect failure to unmarshal
					assertThrows(expected, () -> marshaller.read(buffer), subject::toString);
				} catch (IOException e) {
					assertInstanceOf(expected, e);
				}
			}
		};
	}

	ByteBufferMarshaller getMarshaller();
}
