/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.infinispan;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.impl.HotRodURI;
import org.infinispan.commons.util.Version;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;
import org.wildfly.clustering.container.DefaultContainer;
import org.wildfly.clustering.container.junit.ContainerExtension;
import org.wildfly.clustering.function.Function;

/**
 * JUnit extension that manages the lifecycle of an Infinispan server container and configures a HotRod client.
 * @author Paul Ferraro
 */
public class InfinispanServerExtension extends ContainerExtension<DefaultContainer> {

	static final String DOCKER_NETWORK_MODE_PROPERTY = "docker.network.mode";
	static final String DOCKER_IMAGE_PROPERTY = "infinispan.server.image";
	static final String HOTROD_PORT_PROPERTY = "infinispan.server.port";
	static final String INFINISPAN_CONFIGURATION_PROPERTY = "infinispan.server.configuration";
	static final String INFINISPAN_AUTHENTICATION_PROPERTY = "infinispan.server.authentication";
	static final String INFINISPAN_USERNAME_PROPERTY = "infinispan.server.username";
	static final String INFINISPAN_PASSWORD_PROPERTY = "infinispan.server.password";

	private static final String DEFAULT_DOCKER_IMAGE = "quay.io/infinispan/server:" + Version.getVersion();
	private static final String CONTAINER_IMAGE_CONFIGURATION_PATH = "/opt/infinispan/server/conf/infinispan.xml";
	private static final Boolean DEFAULT_AUTHENTICATION = Boolean.TRUE;
	private static final String DEFAULT_HOTROD_USERNAME = "admin";
	private static final String DEFAULT_HOTROD_PASSWORD = "changeme";
	private static final String USERNAME_ENV = "USER";
	private static final String PASSWORD_ENV = "PASS";
	private static final String CLIENT_INTELLIGENCE = ConfigurationProperties.CLIENT_INTELLIGENCE.substring(ConfigurationProperties.ICH.length());

	/**
	 * Creates an Infinispan OCI container extension.
	 */
	public InfinispanServerExtension() {
		super(new Function<>() {
			@Override
			public DefaultContainer apply(ExtensionContext context) {
				DefaultContainer container = new DefaultContainer(context.getConfigurationParameter(DOCKER_IMAGE_PROPERTY).orElse(DEFAULT_DOCKER_IMAGE));

				context.getConfigurationParameter(DOCKER_NETWORK_MODE_PROPERTY).ifPresent(container::setNetworkMode);

				container.addExposedPort(context.getConfigurationParameter(HOTROD_PORT_PROPERTY, Integer::parseInt).orElse(ConfigurationProperties.DEFAULT_HOTROD_PORT));

				if (container.isPortMapping()) {
					container.setWaitStrategy(Wait.forLogMessage(".*\\QISPN080001\\E.*", 1));
				}
				if (context.getConfigurationParameter(INFINISPAN_AUTHENTICATION_PROPERTY, Boolean::valueOf).orElse(DEFAULT_AUTHENTICATION)) {
					container.addEnv(USERNAME_ENV, context.getConfigurationParameter(INFINISPAN_USERNAME_PROPERTY).orElse(DEFAULT_HOTROD_USERNAME));
					container.addEnv(PASSWORD_ENV, context.getConfigurationParameter(INFINISPAN_PASSWORD_PROPERTY).orElse(DEFAULT_HOTROD_PASSWORD));
				}
				context.getConfigurationParameter(INFINISPAN_CONFIGURATION_PROPERTY).ifPresent(configuration -> {
					// Replace default configuration file within container
					container.withCopyFileToContainer(MountableFile.forHostPath(configuration), CONTAINER_IMAGE_CONFIGURATION_PATH);
				});
				return container;
			}
		});
	}

	/**
	 * Returns a HotRod URI for establishing HotRod connections.
	 * @return a HotRod URI for establishing HotRod connections.
	 */
	public HotRodURI getURI() {
		DefaultContainer container = this.getContainer();
		Map<String, String> env = container.getEnvMap();
		String username = env.get(USERNAME_ENV);
		String password = env.get(PASSWORD_ENV);
		String userInfo = (username != null) && (password != null) ? String.join(":", username, password) : null;
		ClientIntelligence clientIntelligence = container.isPortMapping() ? ClientIntelligence.BASIC : ClientIntelligence.HASH_DISTRIBUTION_AWARE;
		try {
			return HotRodURI.create(new URI("hotrod", userInfo, container.getHost(), container.isPortMapping() ? container.getMappedPort(container.getExposedPorts().get(0)) : container.getExposedPorts().get(0), null, String.join("=", CLIENT_INTELLIGENCE, clientIntelligence.name()), null));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
