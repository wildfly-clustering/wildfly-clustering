/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.impl.HotRodURI;
import org.infinispan.commons.util.Version;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.wildfly.clustering.function.Supplier;

/**
 * Infinispan server test container.
 * @author Paul Ferraro
 */
public class InfinispanServerContainer extends GenericContainer<InfinispanServerContainer> implements Supplier<HotRodURI> {

	static final System.Logger LOGGER = System.getLogger(InfinispanServerContainer.class.getName());

	static final String DOCKER_NETWORK_MODE_PROPERTY = "docker.network.mode";
	static final String DOCKER_IMAGE_PROPERTY = "infinispan.server.image";
	static final String HOTROD_PORT_PROPERTY = "infinispan.server.port";
	static final String INFINISPAN_CONFIGURATION_PROPERTY = "infinispan.server.configuration";
	static final String INFINISPAN_AUTHENTICATION_PROPERTY = "infinispan.server.authentication";
	static final String INFINISPAN_USERNAME_PROPERTY = "infinispan.server.username";
	static final String INFINISPAN_PASSWORD_PROPERTY = "infinispan.server.password";

	private static final String DEFAULT_DOCKER_IMAGE = "quay.io/infinispan/server:" + Version.getVersion();
	private static final String CONTAINER_IMAGE_CONFIGURATION_PATH = "/opt/infinispan/server/conf/infinispan.xml";
	private static final int DEFAULT_HOTROD_PORT = 11222;
	private static final String HOST_NETWORK_MODE = "host";
	private static final String DEFAULT_NETWORK_MODE = "bridge";
	private static final Boolean DEFAULT_AUTHENTICATION = Boolean.TRUE;
	private static final String DEFAULT_HOTROD_USERNAME = "admin";
	private static final String DEFAULT_HOTROD_PASSWORD = "changeme";
	private static final String USERNAME_ENV = "USER";
	private static final String PASSWORD_ENV = "PASS";
	private static final String CLIENT_INTELLIGENCE = ConfigurationProperties.CLIENT_INTELLIGENCE.substring(ConfigurationProperties.ICH.length());

	private final int port;

	InfinispanServerContainer(ExtensionContext context) {
		super(DockerImageName.parse(context.getConfigurationParameter(DOCKER_IMAGE_PROPERTY).orElse(DEFAULT_DOCKER_IMAGE)));

		this.port = context.getConfigurationParameter(HOTROD_PORT_PROPERTY, Integer::parseInt).orElse(DEFAULT_HOTROD_PORT);
		this.setNetworkMode(context.getConfigurationParameter(DOCKER_NETWORK_MODE_PROPERTY).orElse(DEFAULT_NETWORK_MODE));
		if (this.isPortMapping()) {
			this.setExposedPorts(java.util.List.of(this.port));
		}
		this.setHostAccessible(true);
		Map<OutputFrame.OutputType, Optional<PrintStream>> outputs = new EnumMap<>(OutputFrame.OutputType.class);
		outputs.put(OutputFrame.OutputType.END, Optional.empty());
		outputs.put(OutputFrame.OutputType.STDERR, Optional.of(System.err));
		outputs.put(OutputFrame.OutputType.STDOUT, Optional.of(System.out));

		this.withLogConsumer(frame -> outputs.get(frame.getType()).ifPresent(stream -> stream.println(frame.getUtf8StringWithoutLineEnding())));
		// Wait until we can connect to the exposed ports of the container
		if (this.isPortMapping()) {
			// If the port is mapped, just wait for log message
			this.setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*\\QISPN080001\\E.*").withTimes(1).withStartupTimeout(Duration.ofMinutes(1)));
		} else {
			// Otherwise, wait until port is available.
			this.setWaitStrategy(new HostPortWaitStrategy().forPorts(this.port).withStartupTimeout(Duration.ofMinutes(1)));
		}
		if (context.getConfigurationParameter(INFINISPAN_AUTHENTICATION_PROPERTY, Boolean::valueOf).orElse(DEFAULT_AUTHENTICATION)) {
			this.withEnv(USERNAME_ENV, context.getConfigurationParameter(INFINISPAN_USERNAME_PROPERTY).orElse(DEFAULT_HOTROD_USERNAME));
			this.withEnv(PASSWORD_ENV, context.getConfigurationParameter(INFINISPAN_PASSWORD_PROPERTY).orElse(DEFAULT_HOTROD_PASSWORD));
		}
		String configuration = context.getConfigurationParameter(INFINISPAN_CONFIGURATION_PROPERTY).orElse(null);
		if (configuration != null) {
			// Replace default configuration file within container
			this.withCopyFileToContainer(MountableFile.forHostPath(configuration), CONTAINER_IMAGE_CONFIGURATION_PATH);
		}
	}

	private boolean isPortMapping() {
		return !this.getNetworkMode().equals(HOST_NETWORK_MODE);
	}

	@Override
	public HotRodURI get() {
		String username = this.getEnvMap().get(USERNAME_ENV);
		String password = this.getEnvMap().get(PASSWORD_ENV);
		String userInfo = (username != null) && (password != null) ? String.join(":", username, password) : null;
		ClientIntelligence clientIntelligence = this.isPortMapping() ? ClientIntelligence.BASIC : ClientIntelligence.HASH_DISTRIBUTION_AWARE;
		try {
			return HotRodURI.create(new URI("hotrod", userInfo, this.getHost(), this.isPortMapping() ? this.getMappedPort(this.port) : this.port, null, String.join("=", CLIENT_INTELLIGENCE, clientIntelligence.name()), null));
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
