/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.marshall;

import org.infinispan.commons.marshall.ImmutableProtoStreamMarshaller;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.marshall.StreamAwareMarshaller;
import org.infinispan.commons.marshall.StreamingMarshaller;
import org.infinispan.factories.AbstractComponentFactory;
import org.infinispan.factories.AutoInstantiableFactory;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.factories.annotations.DefaultFactoryFor;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.impl.ComponentAlias;
import org.infinispan.factories.impl.ComponentRef;
import org.infinispan.marshall.core.impl.DelegatingUserMarshaller;
import org.infinispan.marshall.persistence.impl.PersistenceMarshallerImpl;
import org.infinispan.marshall.protostream.impl.SerializationContextRegistry;

/**
 * Custom marshaller factory that overrides Infinispan's global marshaller.
 * @author Paul Ferraro
 */
@SuppressWarnings("removal")
@DefaultFactoryFor(classes = { Marshaller.class, StreamingMarshaller.class, StreamAwareMarshaller.class }, names = { KnownComponentNames.INTERNAL_MARSHALLER, KnownComponentNames.PERSISTENCE_MARSHALLER, KnownComponentNames.USER_MARSHALLER })
public class MarshallerFactory extends AbstractComponentFactory implements AutoInstantiableFactory {

	@Inject
	ComponentRef<SerializationContextRegistry> contextRegistry;

	@Override
	public Object construct(String componentName) {

		if (componentName.equals(StreamingMarshaller.class.getName())) {
			return ComponentAlias.of(KnownComponentNames.INTERNAL_MARSHALLER);
		}

		switch (componentName) {
			case KnownComponentNames.PERSISTENCE_MARSHALLER:
				return new PersistenceMarshallerImpl();
			case KnownComponentNames.INTERNAL_MARSHALLER:
				return new GlobalMarshaller();
			case KnownComponentNames.USER_MARSHALLER:
				Marshaller marshaller = this.globalConfiguration.serialization().marshaller();
				if (marshaller != null) {
					marshaller.initialize(this.globalComponentRegistry.getCacheManager().getClassAllowList());
				} else {
					marshaller = new ImmutableProtoStreamMarshaller(this.contextRegistry.wired().getUserCtx());
				}
				return new DelegatingUserMarshaller(marshaller);
			default:
				throw new IllegalArgumentException(componentName);
		}
	}
}
