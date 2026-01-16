/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

import org.wildfly.clustering.marshalling.Marshaller;

/**
 * Generic strategies for marshalling a string-based session identifier.
 * @author Paul Ferraro
 */
public enum IdentifierMarshaller implements Marshaller<String, ByteBuffer> {
	/** Marshals session identifier as a ISO 8859 encoded string */
	ISO_LATIN_1() {
		@Override
		public String read(ByteBuffer original) {
			boolean hasArray = original.hasArray();
			ByteBuffer buffer = hasArray ? original : original.duplicate();
			int length = buffer.remaining();
			int offset = hasArray ? buffer.arrayOffset() + buffer.position() : 0;
			byte[] bytes = buffer.hasArray() ? buffer.array() : new byte[length];
			if (hasArray) {
				buffer.get(bytes, offset, length);
			}
			return new String(bytes, offset, length, StandardCharsets.ISO_8859_1);
		}

		@Override
		public ByteBuffer write(String value) {
			return ByteBuffer.wrap(value.getBytes(StandardCharsets.ISO_8859_1));
		}

		@Override
		public boolean validate(String id) {
			return true;
		}
	},
	/** Marshals session identifier as a Base64-decoded string */
	BASE64() {
		@Override
		public String read(ByteBuffer buffer) throws IOException {
			return ISO_LATIN_1.read(Base64.getUrlEncoder().encode(buffer));
		}

		@Override
		public ByteBuffer write(String value) throws IOException {
			return Base64.getUrlDecoder().decode(ISO_LATIN_1.write(value));
		}
	},
	/** Marshals session identifier as an upper case hex-decoded string */
	HEX_UPPER() {
		private final Marshaller<String, ByteBuffer> marshaller = new HexMarshaller(HexFormat.of().withUpperCase());

		@Override
		public String read(ByteBuffer buffer) throws IOException {
			return this.marshaller.read(buffer);
		}

		@Override
		public ByteBuffer write(String value) throws IOException {
			return this.marshaller.write(value);
		}
	},
	/** Marshals session identifier as a lower case hex-decoded string */
	HEX_LOWER() {
		private final Marshaller<String, ByteBuffer> marshaller = new HexMarshaller(HexFormat.of().withLowerCase());

		@Override
		public String read(ByteBuffer buffer) throws IOException {
			return this.marshaller.read(buffer);
		}

		@Override
		public ByteBuffer write(String value) throws IOException {
			return this.marshaller.write(value);
		}
	}
	;

	@Override
	public boolean test(Object object) {
		return object instanceof String;
	}

	/**
	 * Indicates whether or not the specified identifier is valid for this serializer.
	 * @param id an identifier
	 * @return true, if the specified identifier is valid, false otherwise.
	 */
	public boolean validate(String id) {
		try {
			this.write(id);
			return true;
		} catch (IOException | IllegalArgumentException e) {
			return false;
		}
	}

	static class HexMarshaller implements Marshaller<String, ByteBuffer> {
		private final HexFormat format;

		HexMarshaller(HexFormat format) {
			this.format = format;
		}

		@Override
		public String read(ByteBuffer original) {
			boolean hasArray = original.hasArray();
			ByteBuffer buffer = hasArray ? original : original.duplicate();
			int length = buffer.remaining();
			int offset = hasArray ? buffer.arrayOffset() + buffer.position() : 0;
			byte[] bytes = buffer.hasArray() ? buffer.array() : new byte[length];
			if (hasArray) {
				buffer.get(bytes, offset, length);
			}
			return this.format.formatHex(bytes, offset, length);
		}

		@Override
		public ByteBuffer write(String value) {
			return ByteBuffer.wrap(this.format.parseHex(value));
		}

		@Override
		public boolean test(Object object) {
			return object instanceof String;
		}
	}
}
