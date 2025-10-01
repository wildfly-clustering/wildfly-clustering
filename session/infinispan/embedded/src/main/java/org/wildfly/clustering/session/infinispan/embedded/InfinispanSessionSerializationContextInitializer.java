/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.List;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.AbstractSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.CompositeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContext;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;
import org.wildfly.clustering.session.cache.affinity.SessionAffinitySerializationContextInitializer;
import org.wildfly.clustering.session.cache.attributes.fine.FineSessionAttributesSerializationContextInitializer;
import org.wildfly.clustering.session.cache.metadata.coarse.CoarseSessionMetaDataSerializationContextInitializer;
import org.wildfly.clustering.session.cache.user.UserSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.embedded.attributes.SessionAttributesSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.embedded.user.InfinispanUserSerializationContextInitializer;

/**
 * The serialization context initializer for this module.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanSessionSerializationContextInitializer extends CompositeSerializationContextInitializer {
	/**
	 * Creates the serialization context for this module.
	 */
	public InfinispanSessionSerializationContextInitializer() {
		super(List.of(
				new AbstractSerializationContextInitializer() {
					@Override
					public void registerMarshallers(SerializationContext context) {
						context.registerMarshaller(ProtoStreamMarshaller.of(SessionCacheKeyFilter.class));
						context.registerMarshaller(ProtoStreamMarshaller.of(SessionCacheEntryFilter.class));
					}
				},
				new SessionAffinitySerializationContextInitializer(),
				new CoarseSessionMetaDataSerializationContextInitializer(),
				new FineSessionAttributesSerializationContextInitializer(),
				new SessionMetaDataSerializationContextInitializer(),
				new SessionAttributesSerializationContextInitializer(),
				new UserSerializationContextInitializer(),
				new InfinispanUserSerializationContextInitializer()));
	}
}
