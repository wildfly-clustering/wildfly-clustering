/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * {@link Formatter} implementation for binary types.
 * @param <T> the formatted type
 * @author Paul Ferraro
 */
public class BinaryFormatter<T> implements Formatter<T> {

	private final Class<T> targetClass;
	private final Serializer<T> serializer;

	public BinaryFormatter(Class<T> targetClass, Serializer<T> serializer) {
		this.targetClass = targetClass;
		this.serializer = serializer;
	}

	@Override
	public Class<T> getTargetClass() {
		return this.targetClass;
	}

	@Override
	public T parse(String value) {
		byte[] bytes = Base64.getDecoder().decode(value);
		try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
			return this.serializer.read(input);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public String format(T key) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			this.serializer.write(output, key);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return Base64.getEncoder().encodeToString(bytes.toByteArray());
	}
}
