/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import org.infinispan.commons.dataconversion.DefaultTranscoder;
import org.infinispan.commons.dataconversion.TranscoderMarshallerAdapter;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.encoding.ProtostreamTranscoder;
import org.infinispan.factories.AbstractComponentFactory;
import org.infinispan.factories.AutoInstantiableFactory;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.annotations.ComponentName;
import org.infinispan.factories.annotations.DefaultFactoryFor;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.impl.ComponentRef;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.protostream.impl.SerializationContextRegistry;

/**
 * Overrides Infinispan's default encoder registry.
 * @author Paul Ferraro
 */
@DefaultFactoryFor(classes = { org.infinispan.marshall.core.EncoderRegistry.class })
public class EncoderRegistryFactory extends AbstractComponentFactory implements AutoInstantiableFactory {

	// Must not start the global marshaller or it will be too late for modules to register their externalizers
	@Inject @ComponentName(KnownComponentNames.INTERNAL_MARSHALLER)
	ComponentRef<Marshaller> internalMarshaller;

	@Inject @ComponentName(KnownComponentNames.USER_MARSHALLER)
	Marshaller marshaller;

	@Inject EmbeddedCacheManager manager;
	@Inject SerializationContextRegistry ctxRegistry;

	@Override
	public Object construct(String componentName) {
		ClassLoader classLoader = this.globalConfiguration.classLoader();
		EncoderRegistry encoderRegistry = new DefaultEncoderRegistry();

		// Default and binary transcoder use the user marshaller to convert data to/from a byte array
		encoderRegistry.registerTranscoder(new DefaultTranscoder(this.marshaller));
		// Core transcoders are always available
		encoderRegistry.registerTranscoder(new ProtostreamTranscoder(this.ctxRegistry, classLoader));
		// Wraps the GlobalMarshaller so that it can be used as a transcoder
		// Keeps application/x-infinispan-marshalling available for backwards compatibility
		encoderRegistry.registerTranscoder(new TranscoderMarshallerAdapter(this.internalMarshaller.wired()));
		// Make the user marshaller's media type available as well
		encoderRegistry.registerTranscoder(new TranscoderMarshallerAdapter(this.marshaller));

		return encoderRegistry;
	}
}
