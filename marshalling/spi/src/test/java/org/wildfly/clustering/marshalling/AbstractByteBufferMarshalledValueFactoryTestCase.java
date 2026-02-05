/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ByteBufferMarshalledValue}.
 *
 * @author Brian Stansberry
 * @author Paul Ferraro
 */
public abstract class AbstractByteBufferMarshalledValueFactoryTestCase {

	private final ByteBufferMarshaller marshaller;
	private final ByteBufferMarshalledValueFactory factory;

	protected AbstractByteBufferMarshalledValueFactoryTestCase(MarshallingTesterFactory factory) {
		this(factory.getMarshaller());
	}

	protected AbstractByteBufferMarshalledValueFactoryTestCase(ByteBufferMarshaller marshaller) {
		this(marshaller, new ByteBufferMarshalledValueFactory(marshaller));
	}

	AbstractByteBufferMarshalledValueFactoryTestCase(ByteBufferMarshaller marshaller, ByteBufferMarshalledValueFactory factory) {
		this.marshaller = marshaller;
		this.factory = factory;
	}

	@Test
	public void get() throws Exception {
		UUID uuid = UUID.randomUUID();
		ByteBufferMarshalledValue<UUID> mv = this.factory.createMarshalledValue(uuid);

		assertThat(mv.peek()).isNotNull().isSameAs(uuid);
		assertThat(mv.get(this.marshaller)).isNotNull().isSameAs(uuid);

		ByteBufferMarshalledValue<UUID> copy = replicate(mv);

		assertThat(copy).isNotNull();
		assertThat(copy.peek()).isNull();

		UUID uuid2 = copy.get(this.marshaller);
		assertThat(uuid2).isNotSameAs(uuid).isEqualTo(uuid);

		copy = replicate(copy);
		uuid2 = copy.get(this.marshaller);
		assertThat(uuid2).isEqualTo(uuid);

		mv = this.factory.createMarshalledValue(null);
		assertThat(mv).isNotNull();
		assertThat(mv.peek()).isNull();
		assertThat(mv.getBuffer()).isNull();
		assertThat(mv.get(this.marshaller)).isNull();
	}

	@Test
	public void equals() throws Exception {
		UUID uuid = UUID.randomUUID();
		ByteBufferMarshalledValue<UUID> mv = this.factory.createMarshalledValue(uuid);

		assertThat(mv).isNotNull().isEqualTo(mv);

		ByteBufferMarshalledValue<UUID> dup = this.factory.createMarshalledValue(uuid);
		assertThat(dup).isEqualTo(mv);
		assertThat(mv).isEqualTo(dup);

		ByteBufferMarshalledValue<UUID> replica = replicate(mv);
		assertThat(replica).isEqualTo(mv);
		assertThat(mv).isEqualTo(replica);

		ByteBufferMarshalledValue<UUID> nulled = this.factory.createMarshalledValue(null);
		assertThat(nulled).isNotNull().isNotEqualTo(mv).isNotEqualTo(replica).isEqualTo(nulled);
		assertThat(mv).isNotEqualTo(nulled);
		assertThat(replica).isNotEqualTo(nulled);
		assertThat(this.factory.createMarshalledValue(null)).isEqualTo(nulled);
	}

	@Test
	public void testHashCode() throws Exception {
		UUID uuid = UUID.randomUUID();
		ByteBufferMarshalledValue<UUID> mv = this.factory.createMarshalledValue(uuid);
		assertThat(mv).hasSameHashCodeAs(uuid);

		ByteBufferMarshalledValue<UUID> copy = replicate(mv);
		assertThat(copy.hashCode()).isEqualTo(0);

		mv = this.factory.createMarshalledValue(null);
		assertThat(mv.hashCode()).isEqualTo(0);
	}

	@SuppressWarnings("unchecked")
	<V> ByteBufferMarshalledValue<V> replicate(ByteBufferMarshalledValue<V> value) throws IOException {
		OptionalInt size = this.marshaller.size(value);
		ByteBuffer buffer = this.marshaller.write(value);
		if (size.isPresent()) {
			// Verify that computed size equals actual size
			assertThat(size).hasValue(buffer.remaining());
		}
		ByteBufferMarshalledValue<V> result = (ByteBufferMarshalledValue<V>) this.marshaller.read(buffer);
		OptionalInt resultSize = this.marshaller.size(result);
		if (size.isPresent() && resultSize.isPresent()) {
			// Verify that computed size equals actual size
			assertThat(resultSize).isEqualTo(size);
		}
		return result;
	}
}
