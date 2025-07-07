/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import java.util.List;

import org.infinispan.commons.configuration.ClassAllowList;
import org.infinispan.commons.dataconversion.DefaultTranscoder;
import org.infinispan.commons.dataconversion.TranscoderMarshallerAdapter;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.encoding.ProtostreamTranscoder;
import org.infinispan.encoding.impl.JavaSerializationTranscoder;
import org.infinispan.factories.AbstractComponentFactory;
import org.infinispan.factories.AutoInstantiableFactory;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.annotations.ComponentName;
import org.infinispan.factories.annotations.DefaultFactoryFor;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.protostream.impl.SerializationContextRegistry;

/**
 * Overrides Infinispan's default encoder registry.
 * @author Paul Ferraro
 */
@DefaultFactoryFor(classes = { org.infinispan.marshall.core.EncoderRegistry.class })
public class EncoderRegistryFactory extends AbstractComponentFactory implements AutoInstantiableFactory {

	@Inject @ComponentName(KnownComponentNames.USER_MARSHALLER)
	Marshaller marshaller;

	@Inject EmbeddedCacheManager manager;
	@Inject SerializationContextRegistry registry;

	@Override
	public Object construct(String componentName) {
		ClassLoader classLoader = this.globalConfiguration.classLoader();
		ClassAllowList classAllowList = this.manager.getClassAllowList();

		return new DefaultEncoderRegistry(List.of(
				new DefaultTranscoder(this.marshaller),
				new TranscoderMarshallerAdapter(this.marshaller),
				new ProtostreamTranscoder(this.registry, classLoader),
				new JavaSerializationTranscoder(classAllowList)));
	}
}
