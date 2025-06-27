/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.java;

import java.io.ObjectInputFilter;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;

/**
 * @author Paul Ferraro
 */
@MetaInfServices({ MarshallingTesterFactory.class, JavaSerializationTesterFactory.class })
public class JavaSerializationTesterFactory implements MarshallingTesterFactory {

	private final ByteBufferMarshaller marshaller;

	public JavaSerializationTesterFactory() {
		this(ClassLoader.getSystemClassLoader(), null);
	}

	public JavaSerializationTesterFactory(ClassLoader loader, ObjectInputFilter filter) {
		this.marshaller = new JavaByteBufferMarshaller(loader, filter);
	}

	@Override
	public ByteBufferMarshaller getMarshaller() {
		return this.marshaller;
	}

	@Override
	public String toString() {
		return this.marshaller.toString();
	}
}
