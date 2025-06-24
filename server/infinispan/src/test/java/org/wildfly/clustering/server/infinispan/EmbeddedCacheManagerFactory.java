/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.sql.DataSource;

import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.globalstate.ConfigurationStorage;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.remoting.transport.jgroups.JGroupsChannelConfigurator;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.JChannel;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.util.SocketFactory;
import org.wildfly.clustering.cache.infinispan.marshalling.MediaTypes;
import org.wildfly.clustering.cache.infinispan.marshalling.UserMarshaller;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContextBuilder;

/**
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerFactory implements BiFunction<String, ClassLoader, EmbeddedCacheManager> {

	private final Function<String, JChannel> factory;
	private final String clusterName;
	private final String memberName;

	public EmbeddedCacheManagerFactory(Function<String, JChannel> factory, String clusterName, String memberName) {
		this.factory = factory;
		this.clusterName = clusterName;
		this.memberName = memberName;
	}

	@Override
	public EmbeddedCacheManager apply(String name, ClassLoader loader) {
		Marshaller marshaller = new UserMarshaller(MediaTypes.WILDFLY_PROTOSTREAM, new ProtoStreamByteBufferMarshaller(SerializationContextBuilder.newInstance(ClassLoaderMarshaller.of(loader)).load(loader).build()));
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
			public void setDataSource(DataSource dataSource) {
			}
		};
		GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder().cacheManagerName(name).classLoader(loader);
		try {
			builder.globalState()
					.configurationStorage(ConfigurationStorage.VOLATILE)
					.persistentLocation(Files.createTempDirectory(name).toString(), name)
					.temporaryLocation(Files.createTempDirectory(name).toString(), name)
					;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		builder.transport().defaultTransport().clusterName(this.clusterName).nodeName(this.memberName).addProperty(JGroupsTransport.CHANNEL_CONFIGURATOR, configurator);
		// Register dummy serialization context initializer, to bypass service loading in org.infinispan.marshall.protostream.impl.SerializationContextRegistryImpl
		builder.serialization().marshaller(marshaller).addContextInitializer(new SerializationContextInitializer() {
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
		});
		return new DefaultCacheManager(new ConfigurationBuilderHolder(loader, builder), false);
	}
}
