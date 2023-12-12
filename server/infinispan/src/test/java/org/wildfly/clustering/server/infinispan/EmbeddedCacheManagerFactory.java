/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.remoting.transport.jgroups.JGroupsChannelConfigurator;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.util.SocketFactory;
import org.wildfly.clustering.cache.infinispan.marshalling.protostream.ProtoStreamMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;

/**
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerFactory implements Function<String, EmbeddedCacheManager> {

	private final Function<String, JChannel> factory;
	private final String clusterName;
	private final String memberName;

	public EmbeddedCacheManagerFactory(Function<String, JChannel> factory, String clusterName, String memberName) {
		this.factory = factory;
		this.clusterName = clusterName;
		this.memberName = memberName;
	}

	@Override
	public EmbeddedCacheManager apply(String name) {
		Marshaller marshaller = new ProtoStreamMarshaller(ClassLoaderMarshaller.of(this.getClass().getClassLoader()), UnaryOperator.identity());
		JGroupsChannelConfigurator configurator = new JGroupsChannelConfigurator() {
			@Override
			public String getProtocolStackString() {
				return null;
			}

			@Override
			public List<ProtocolConfiguration> getProtocolStack() {
				return null;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public JChannel createChannel(String ignored) throws Exception {
				return EmbeddedCacheManagerFactory.this.factory.apply(name);
			}

			@Override
			public void setSocketFactory(SocketFactory socketFactory) {
			}

			@Override
			public void addChannelListener(ChannelListener listener) {
			}
		};
		GlobalConfiguration global = new GlobalConfigurationBuilder().cacheManagerName(name)
				.transport().defaultTransport().clusterName(this.clusterName).nodeName(this.memberName).addProperty(JGroupsTransport.CHANNEL_CONFIGURATOR, configurator)
				// Register dummy serialization context initializer, to bypass service loading in org.infinispan.marshall.protostream.impl.SerializationContextRegistryImpl
				.serialization().marshaller(marshaller).addContextInitializer(new SerializationContextInitializer() {
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
				.build();
		return new DefaultCacheManager(global);
	}

}
