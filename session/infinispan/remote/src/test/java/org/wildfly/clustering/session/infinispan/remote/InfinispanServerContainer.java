/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.util.Version;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.OutputFrame.OutputType;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Paul Ferraro
 */
public class InfinispanServerContainer extends GenericContainer<InfinispanServerContainer> implements Function<ConfigurationBuilder, RemoteCacheContainer>, Consumer<OutputFrame> {

	static final DockerImageName DOCKER_IMAGE = DockerImageName.parse(System.getProperty("infinispan.server.image", "quay.io/infinispan/server:" + Version.getMajorMinor()));
	static final Logger LOGGER = Logger.getLogger(InfinispanServerContainer.class);
	static final int DEFAULT_PORT = 11222;
	static final String DEFAULT_USER = "admin";
	static final String DEFAULT_PASSWORD = "changeme";

	InfinispanServerContainer() {
		super(DOCKER_IMAGE);

		this.setNetworkMode("host");
		this.setHostAccessible(true);
		this.withLogConsumer(this);
		// Wait for server started log message
		this.setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*\\QISPN080001\\E.*").withTimes(1).withStartupTimeout(Duration.ofMinutes(2)));
		this.withEnv("USER", DEFAULT_USER);
		this.withEnv("PASS", DEFAULT_PASSWORD);
	}

	@Override
	public void start() {
		this.logger().info("Starting an Infinispan server container using [{}]", this.getDockerImageName());
		super.start();
	}

	@Override
	public RemoteCacheContainer apply(ConfigurationBuilder builder) {
		builder.addServer().host(this.getHost()).port(DEFAULT_PORT).security().authentication()
			.username(this.getEnvMap().get("USER"))
			.password(this.getEnvMap().get("PASS"))
			;
		return new RemoteCacheManager(builder.build(), false);
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
