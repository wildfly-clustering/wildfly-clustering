/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.OptionalInt;

/**
 * A {@link ByteBufferMarshaller} based {@link TestMarshaller}.
 * @param <T> marshaller target type
 * @author Paul Ferraro
 */
public class ByteBufferTestMarshaller<T> implements TestMarshaller<T> {

	private final ByteBufferMarshaller marshaller;

	public ByteBufferTestMarshaller(ByteBufferMarshaller marshaller) {
		this.marshaller = marshaller;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T read(ByteBuffer buffer) throws IOException {
		return (T) this.marshaller.read(buffer);
	}

	@Override
	public ByteBuffer write(T object) throws IOException {
		ByteBuffer buffer = this.marshaller.write(object);
		OptionalInt size = this.marshaller.size(object);
		if (size.isPresent()) {
			assertThat(size).isEqualTo(buffer.remaining());
		}
		return buffer;
	}

	@Override
	public boolean isMarshallable(Object object) {
		return this.marshaller.isMarshallable(object);
	}
}
