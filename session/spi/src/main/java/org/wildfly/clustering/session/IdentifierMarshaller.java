/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.wildfly.clustering.marshalling.Marshaller;

/**
 * Generic strategies for marshalling a string-based session identifier.
 * @author Paul Ferraro
 */
public enum IdentifierMarshaller implements Marshaller<String, ByteBuffer> {
	ISO_LATIN_1() {
		@Override
		public String read(ByteBuffer buffer) throws IOException {
			if (!buffer.hasArray()) {
				throw new IllegalArgumentException(buffer.toString());
			}
			int offset = buffer.arrayOffset();
			int length = buffer.limit() - offset;
			return new String(buffer.array(), offset, length, StandardCharsets.ISO_8859_1);
		}

		@Override
		public ByteBuffer write(String value) throws IOException {
			return ByteBuffer.wrap(value.getBytes(StandardCharsets.ISO_8859_1));
		}

		@Override
		public boolean validate(String id) {
			return true;
		}
	},
	/**
	 * Specific optimization for Base64-encoded identifiers (e.g. Undertow).
	 */
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
	/**
	 * Specific optimization for hex-encoded identifiers (e.g. Tomcat).
	 */
	HEX() {
		// JDK17
/*
		private final HexFormat format = HexFormat.of().withUpperCase();

		@Override
		public String read(ByteBuffer buffer) throws IOException {
			if (!buffer.hasArray()) {
				throw new IllegalArgumentException(buffer.toString());
			}
			return this.format.formatHex(buffer.array(), buffer.arrayOffset(), buffer.limit());
		}

		@Override
		public ByteBuffer write(String value) throws IOException {
			return ByteBuffer.wrap(this.format.parseHex(value));
		}
*/
		@Override
		public String read(ByteBuffer buffer) throws IOException {
			if (!buffer.hasArray()) {
				throw new IllegalArgumentException(buffer.toString());
			}
			int offset = buffer.arrayOffset();
			int length = buffer.limit() - offset;
			StringBuilder builder = new StringBuilder(length * 2);
			while (buffer.hasRemaining()) {
				byte b = buffer.get();
				builder.append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xf, 16)));
				builder.append(Character.toUpperCase(Character.forDigit(b & 0xf, 16)));
			}
			return builder.toString();
		}

		@Override
		public ByteBuffer write(String value) throws IOException {
			if (value.length() % 2 != 0) {
				throw new IllegalArgumentException(value);
			}
			byte[] bytes = new byte[value.length() / 2];
			for (int i = 0; i < bytes.length; ++i) {
				int index = i * 2;
				int high = Character.digit(value.charAt(index), 16) << 4;
				int low = Character.digit(value.charAt(index + 1), 16);
				bytes[i] = (byte) (high + low);
			}
			return ByteBuffer.wrap(bytes);
		}
	},
	;

	@Override
	public boolean isMarshallable(Object object) {
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
}
