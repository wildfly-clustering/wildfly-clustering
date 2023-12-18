/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import java.util.List;

import org.jboss.marshalling.MarshallingConfiguration;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;
import org.wildfly.clustering.marshalling.java.JavaTesterFactory;

/**
 * @author Paul Ferraro
 */
public enum JBossTesterFactory implements MarshallingTesterFactory {
	INSTANCE;

	private final ByteBufferMarshaller marshaller;

	JBossTesterFactory() {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setClassTable(new LoadedClassTable(loader));
		configuration.setObjectTable(new LoadedObjectTable(loader));
		this.marshaller = new JBossByteBufferMarshaller(MarshallingConfigurationRepository.from(configuration), loader);
	}

	@Override
	public ByteBufferMarshaller getMarshaller() {
		return this.marshaller;
	}

	@Override
	public List<ByteBufferMarshaller> getBenchmarkMarshallers() {
		return List.of(JavaTesterFactory.INSTANCE.getMarshaller());
	}
}
