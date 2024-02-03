/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.remote;

import java.time.Duration;
import java.util.function.Consumer;

import org.infinispan.commons.util.Version;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.OutputFrame.OutputType;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * Infinispan server test container.
 * @author Paul Ferraro
 */
public class InfinispanServerContainer extends GenericContainer<InfinispanServerContainer> implements Consumer<OutputFrame> {

	static final Logger LOGGER = Logger.getLogger(InfinispanServerContainer.class);

	static final String DOCKER_NETWORK_MODE_PROPERTY = "docker.network.mode";
	static final String DOCKER_IMAGE_PROPERTY = "infinispan.server.image";
	static final String HOTROD_PORT_PROPERTY = "infinispan.server.port";
	static final String HOTROD_USERNAME_PROPERTY = "infinispan.server.username";
	static final String HOTROD_PASSWORD_PROPERTY = "infinispan.server.password";

	private static final String DEFAULT_DOCKER_IMAGE = "quay.io/infinispan/server:" + Version.getMajorMinor();
	private static final int DEFAULT_HOTROD_PORT = 11222;
	private static final String HOST_NETWORK_MODE = "host";
	private static final String DEFAULT_NETWORK_MODE = "bridge";
	private static final String DEFAULT_HOTROD_USERNAME = "admin";
	private static final String DEFAULT_HOTROD_PASSWORD = "changeme";
	private static final String USERNAME_ENV = "USER";
	private static final String PASSWORD_ENV = "PASS";

	private final int port;

	InfinispanServerContainer(ExtensionContext context) {
		super(DockerImageName.parse(context.getConfigurationParameter(DOCKER_IMAGE_PROPERTY).orElse(DEFAULT_DOCKER_IMAGE)));

		this.port = context.getConfigurationParameter(HOTROD_PORT_PROPERTY, Integer::parseInt).orElse(DEFAULT_HOTROD_PORT);
		this.setNetworkMode(context.getConfigurationParameter(DOCKER_NETWORK_MODE_PROPERTY).orElse(DEFAULT_NETWORK_MODE));
		if (!this.getNetworkMode().equals(HOST_NETWORK_MODE)) {
			this.setExposedPorts(java.util.List.of(this.port));
		}
		this.setHostAccessible(true);
		this.withLogConsumer(this);
		// Wait for server started log message
		this.setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*\\QISPN080001\\E.*").withTimes(1).withStartupTimeout(Duration.ofMinutes(2)));
		this.withEnv(USERNAME_ENV, context.getConfigurationParameter(HOTROD_USERNAME_PROPERTY).orElse(DEFAULT_HOTROD_USERNAME));
		this.withEnv(PASSWORD_ENV, context.getConfigurationParameter(HOTROD_PASSWORD_PROPERTY).orElse(DEFAULT_HOTROD_PASSWORD));
	}

	public boolean isPortMapping() {
		return !this.getNetworkMode().equals(HOST_NETWORK_MODE);
	}

	@Override
	public Integer getMappedPort(int originalPort) {
		return this.isPortMapping() ? super.getMappedPort(originalPort) : originalPort;
	}

	public int getPort() {
		return this.getMappedPort(this.port);
	}

	public String getUsername() {
		return this.getEnvMap().get(USERNAME_ENV);
	}

	public String getPassword() {
		return this.getEnvMap().get(PASSWORD_ENV);
	}

	@Override
	public void accept(OutputFrame frame) {
		OutputFrame.OutputType type = frame.getType();
		if (type != OutputType.END) {
			String message = frame.getUtf8String().replaceAll("((\\r?\\n)|(\\r))$", "");
			LOGGER.logf(type == OutputType.STDERR ? Level.ERROR : Level.INFO, message);
		}
	}
}
