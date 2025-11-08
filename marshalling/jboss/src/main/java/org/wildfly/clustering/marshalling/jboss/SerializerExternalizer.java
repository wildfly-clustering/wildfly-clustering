/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.marshalling.Externalizer;
import org.wildfly.clustering.marshalling.Serializer;

/**
 * An {@link Externalizer} decorator for a {@link Serializer}.
 * N.B. This object is <em>not</em> serializable.
 * @author Paul Ferraro
 */
@SuppressWarnings("serial")
public class SerializerExternalizer implements Externalizer {
	/** N.B. This is not serializable. */
	private final Serializer<Object> serializer;

	/**
	 * Creates a new externalizer using the specified serializer.
	 * @param serializer a serializer
	 */
	@SuppressWarnings("unchecked")
	public SerializerExternalizer(Serializer<?> serializer) {
		this.serializer = (Serializer<Object>) serializer;
	}

	@Override
	public void writeExternal(Object subject, ObjectOutput output) throws IOException {
		this.serializer.write(output, subject);
	}

	@Override
	public Object createExternal(Class<?> subjectType, ObjectInput input) throws IOException {
		return this.serializer.read(input);
	}
}
