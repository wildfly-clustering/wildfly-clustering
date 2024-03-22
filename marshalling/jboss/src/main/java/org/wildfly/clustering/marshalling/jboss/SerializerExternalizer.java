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
 * @author Paul Ferraro
 */
public class SerializerExternalizer implements Externalizer {
	private static final long serialVersionUID = 5193048457273732365L;

	private final Serializer<Object> serializer;

	@SuppressWarnings("unchecked")
	public SerializerExternalizer(Serializer<?> serializer) {
		this.serializer = (Serializer<Object>) serializer;
	}

	@Override
	public void writeExternal(Object subject, ObjectOutput output) throws IOException {
		this.serializer.write(output, subject);
	}

	@Override
	public Object createExternal(Class<?> subjectType, ObjectInput input) throws IOException, ClassNotFoundException {
		return this.serializer.read(input);
	}
}
