/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.assertj.core.api.Assertions.*;

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
	System.Logger LOGGER = System.getLogger(MarshallingTesterFactory.class.getName());

	@Override
	default <T> Tester<T> createTester(BiConsumer<T, T> assertion) {
		ByteBufferMarshaller marshaller = this.getMarshaller();
		return new Tester<>() {
			@Override
			public void accept(T subject) {
				try {
					assertThat(marshaller.isMarshallable(subject)).as(() -> Optional.ofNullable(subject).map(Object::toString).orElse(null)).isTrue();

					OptionalInt size = marshaller.size(subject);
					ByteBuffer buffer = marshaller.write(subject);

					int bufferSize = buffer.limit() - buffer.arrayOffset();

					if (size.isPresent()) {
						assertThat(size).hasValue(bufferSize);
					}

					if (subject != null) {
						Class<?> subjectClass = (subject instanceof Enum enumValue) ? enumValue.getDeclaringClass() : subject.getClass();
						Object subjectValue = (subject instanceof Character character) ? (int) character.charValue() : subject;
						LOGGER.log(System.Logger.Level.DEBUG, "{0}\t{1}\t{2}\t{3}", marshaller, subjectClass.getCanonicalName(), subjectValue, bufferSize);
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
				assertThat(marshaller.isMarshallable(subject)).as(subject::toString).isFalse();
			}

			@Override
			public <E extends Throwable> void reject(T subject, Class<E> expected) {
				assertThat(marshaller.isMarshallable(subject)).as(subject::toString).isTrue();
				try {
					ByteBuffer buffer = marshaller.write(subject);
					// If we were able to marshal, expect failure to unmarshal
					assertThatExceptionOfType(expected).as(subject::toString).isThrownBy(() -> marshaller.read(buffer));
				} catch (IOException e) {
					assertThat(e).isInstanceOf(expected);
				}
			}
		};
	}

	ByteBufferMarshaller getMarshaller();
}
