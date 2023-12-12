/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.infinispan.commons.marshall.IdentityMarshaller;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.junit.jupiter.api.Test;

/**
 * @author Paul Ferraro
 */
public class LocalGroupTestCase {

	private static final String GROUP_NAME = "group";
	private static final String MEMBER_NAME = "member";

	@Test
	public void test() throws IOException {
		GlobalConfiguration global = new GlobalConfigurationBuilder().nonClusteredDefault().cacheManagerName(GROUP_NAME)
				.serialization().marshaller(IdentityMarshaller.INSTANCE).addContextInitializer(new SerializationContextInitializer() {
					@Deprecated
					@Override
					public String getProtoFile() {
						return null;
					}

					@Deprecated
					@Override
					public String getProtoFileName() {
						return null;
					}

					@Override
					public void registerMarshallers(SerializationContext context) {
					}

					@Override
					public void registerSchema(SerializationContext context) {
					}
				})
				.transport().nodeName(MEMBER_NAME).build();
		try (EmbeddedCacheManager manager = new DefaultCacheManager(global, true)) {
			LocalEmbeddedCacheManagerGroupConfiguration config = () -> manager;
			CacheContainerGroup group = new EmbeddedCacheManagerGroup<>(config);

			assertSame(GROUP_NAME, group.getName());
			assertSame(MEMBER_NAME, group.getLocalMember().getName());
			assertEquals(List.of(group.getLocalMember()), group.getMembership().getMembers());
			assertSame(group.getLocalMember(), group.getMembership().getCoordinator());
		}
	}
}
