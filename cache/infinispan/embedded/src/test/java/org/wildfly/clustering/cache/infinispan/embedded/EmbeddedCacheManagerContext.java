/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
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
import org.wildfly.clustering.context.AbstractContext;
import org.wildfly.clustering.context.Context;
import org.wildfly.clustering.function.Consumer;
import org.wildfly.clustering.marshalling.protostream.ClassLoaderMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamByteBufferMarshaller;
import org.wildfly.clustering.marshalling.protostream.SerializationContextBuilder;
import org.wildfly.clustering.server.jgroups.ForkChannelFactory;
import org.wildfly.clustering.server.jgroups.JChannelContext;

/**
 * A context providing an {@link EmbeddedCacheManager}.
 * @author Paul Ferraro
 */
public class EmbeddedCacheManagerContext extends AbstractContext<EmbeddedCacheManager> {
	private static final String CONTAINER_NAME = "container";

	private final EmbeddedCacheManager manager;

	public EmbeddedCacheManagerContext(String clusterName, String memberName) {
		this(new JChannelContext(clusterName, memberName));
	}

	public EmbeddedCacheManagerContext(JChannel channel) {
		this(Context.of(channel, Consumer.empty()));
	}

	private EmbeddedCacheManagerContext(Context<JChannel> channel) {
		this.accept(channel::close);
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			Marshaller marshaller = new UserMarshaller(MediaTypes.WILDFLY_PROTOSTREAM, new ProtoStreamByteBufferMarshaller(SerializationContextBuilder.newInstance(ClassLoaderMarshaller.of(loader)).load(loader).build()));
			Function<String, JChannel> channelFactory = new ForkChannelFactory(channel.get());
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
					return CONTAINER_NAME;
				}

				@Override
				public JChannel createChannel(String ignored) throws Exception {
					return channelFactory.apply(CONTAINER_NAME);
				}

				@Override
				public void setSocketFactory(SocketFactory socketFactory) {
				}

				@Override
				public void setDataSource(DataSource dataSource) {
				}
			};
			GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder().cacheManagerName(CONTAINER_NAME).classLoader(loader);
			try {
				builder.globalState()
						.configurationStorage(ConfigurationStorage.VOLATILE)
						.persistentLocation(Files.createTempDirectory(CONTAINER_NAME).toString(), CONTAINER_NAME)
						.temporaryLocation(Files.createTempDirectory(CONTAINER_NAME).toString(), CONTAINER_NAME)
						;
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			builder.transport().defaultTransport().clusterName(channel.get().getClusterName()).nodeName(channel.get().getName()).addProperty(JGroupsTransport.CHANNEL_CONFIGURATOR, configurator);
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
			this.manager = new DefaultCacheManager(new ConfigurationBuilderHolder(loader, builder), false);
			this.manager.start();
			this.accept(this.manager::stop);
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public EmbeddedCacheManager get() {
		return this.manager;
	}
}
