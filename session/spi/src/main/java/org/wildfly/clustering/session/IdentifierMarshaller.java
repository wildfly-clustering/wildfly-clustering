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
	/**
	 * Specific optimization for hex-encoded identifiers (e.g. Tomcat).
	 */
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

	static class HexMarshaller implements Marshaller<String, ByteBuffer> {
		private final HexFormat format;

		HexMarshaller(HexFormat format) {
			this.format = format;
		}

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

		@Override
		public boolean isMarshallable(Object object) {
			return object instanceof String;
		}
	}
}
