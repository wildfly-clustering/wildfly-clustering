/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded;

import java.util.List;

import org.infinispan.protostream.SerializationContextInitializer;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.protostream.CompositeSerializationContextInitializer;
import org.wildfly.clustering.session.cache.attributes.fine.FineSessionAttributesSerializationContextInitializer;
import org.wildfly.clustering.session.cache.metadata.coarse.CoarseSessionMetaDataSerializationContextInitializer;
import org.wildfly.clustering.session.cache.user.UserSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.embedded.attributes.SessionAttributesSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataSerializationContextInitializer;
import org.wildfly.clustering.session.infinispan.embedded.user.InfinispanUserSerializationContextInitializer;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(SerializationContextInitializer.class)
public class InfinispanSessionSerializationContextInitializer extends CompositeSerializationContextInitializer {

	public InfinispanSessionSerializationContextInitializer() {
		super(List.of(
				new CoarseSessionMetaDataSerializationContextInitializer(),
				new FineSessionAttributesSerializationContextInitializer(),
				new SessionMetaDataSerializationContextInitializer(),
				new SessionAttributesSerializationContextInitializer(),
				new UserSerializationContextInitializer(),
				new InfinispanUserSerializationContextInitializer()));
	}
}
