/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.jboss;

import org.jboss.marshalling.MarshallingConfiguration;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshallingTesterFactory;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(MarshallingTesterFactory.class)
public class JBossMarshallingTesterFactory implements MarshallingTesterFactory {

	private final ByteBufferMarshaller marshaller;

	public JBossMarshallingTesterFactory() {
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
	public String toString() {
		return this.marshaller.toString();
	}
}
