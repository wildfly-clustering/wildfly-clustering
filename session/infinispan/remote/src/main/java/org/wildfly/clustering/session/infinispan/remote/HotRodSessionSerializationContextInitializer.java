/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.util.List;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.CompositeSerializationContextInitializer;
import org.wildfly.clustering.marshalling.protostream.SerializationContextInitializer;
import org.wildfly.clustering.session.cache.attributes.fine.FineSessionAttributesSerializationContextInitializer;
import org.wildfly.clustering.session.cache.metadata.fine.FineSessionMetaDataSerializationContextInitializer;
import org.wildfly.clustering.session.cache.user.UserSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.remote.attributes.SessionAttributesSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.remote.metadata.SessionMetaDataSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.remote.user.HotRodUserSerializationContextInitializer;

/**
 * The serialization context initializer for this module.
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class HotRodSessionSerializationContextInitializer extends CompositeSerializationContextInitializer {
	/**
	 * Creates a serialization context initializer for this module.
	 */
	public HotRodSessionSerializationContextInitializer() {
		// Initialize only those marshallers used by this implementation
		super(List.of(
				new FineSessionMetaDataSerializationContextInitializer(),
				new FineSessionAttributesSerializationContextInitializer(),
				new SessionMetaDataSerializationContextInitializer(),
				new SessionAttributesSerializationContextInitializer(),
				new UserSerializationContextInitializer(),
				new HotRodUserSerializationContextInitializer()));
	}
}
