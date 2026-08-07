/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.infinispan;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.impl.HotRodURI;
import org.infinispan.commons.util.Version;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;
import org.wildfly.clustering.container.ContainerResourceLocator;
import org.wildfly.clustering.container.DefaultContainer;
import org.wildfly.clustering.function.Consumer;

/**
 * JUnit extension that manages the lifecycle of one or more Infinispan server containers and configures a HotRod client.
 * @author Paul Ferraro
 */
public class InfinispanServerExtension implements AfterAllCallback, BeforeAllCallback, ContainerResourceLocator<HotRodURI> {

	static final String CONTAINER_IMAGE_PROPERTY = "oci.image";
	static final String INFINISPAN_SERVER_PORT_PROPERTY = "infinispan.server.port";
	static final String INFINISPAN_CLUSTER_SIZE_PROPERTY = "infinispan.cluster.size";
	static final String INFINISPAN_CLUSTER_PORT_OFFSET_PROPERTY = "infinispan.cluster.port-offset";
	static final String INFINISPAN_CONFIGURATION_PROPERTY = "infinispan.server.configuration";
	static final String INFINISPAN_AUTHENTICATION_PROPERTY = "infinispan.server.authentication";
	static final String INFINISPAN_USERNAME_PROPERTY = "infinispan.server.username";
	static final String INFINISPAN_PASSWORD_PROPERTY = "infinispan.server.password";

	private static final AtomicInteger COUNTER = new AtomicInteger(0);

	private static final String DEFAULT_CONTAINER_IMAGE = "quay.io/infinispan/server:" + Version.getVersion();
	private static final String CONTAINER_IMAGE_CONFIGURATION_PATH = "/opt/infinispan/server/conf/infinispan.xml";
	private static final Boolean DEFAULT_AUTHENTICATION = Boolean.TRUE;
	private static final String DEFAULT_HOTROD_USERNAME = "admin";
	private static final String DEFAULT_HOTROD_PASSWORD = "changeme";
	private static final String USERNAME_ENV = "USER";
	private static final String PASSWORD_ENV = "PASS";
	private static final String CLIENT_INTELLIGENCE = ConfigurationProperties.CLIENT_INTELLIGENCE.substring(ConfigurationProperties.ICH.length());

	private final Deque<DefaultContainer> containers;
	private final int defaultClusterSize;

	/**
	 * Creates an Infinispan OCI container extension.
	 */
	public InfinispanServerExtension() {
		this(1);
	}

	/**
	 * Creates an Infinispan OCI container extension that creates a cluster of the specified size.
	 * @param clusterSize the default size of the cluster
	 */
	public InfinispanServerExtension(int clusterSize) {
		this.defaultClusterSize = clusterSize;
		this.containers = new ArrayDeque<>(clusterSize);
	}

	Iterable<DefaultContainer> getContainers() {
		return this.containers;
	}

	@Override
	public HotRodURI getURI() {
		Map<String, String> env = this.containers.element().getEnvMap();
		String username = env.get(USERNAME_ENV);
		String password = env.get(PASSWORD_ENV);
		StringBuilder builder = new StringBuilder("hotrod://");
		if ((username != null) && (password != null)) {
			builder.append(username).append(':').append(password).append('@');
		}
		boolean first = true;
		for (DefaultContainer container : this.containers) {
			for (int port : container.getExposedPorts()) {
				if (first) {
					first = false;
				} else {
					builder.append(',');
				}
				builder.append(container.getHost()).append(':').append(container.getMappedPort(port));
			}
		}
		builder.append('?').append(CLIENT_INTELLIGENCE).append('=').append((this.containers.size() > 1) ? ClientIntelligence.HASH_DISTRIBUTION_AWARE : ClientIntelligence.BASIC);
		return HotRodURI.create(builder.toString());
	}

	@Override
	public void beforeAll(ExtensionContext context) {
		if (COUNTER.getAndIncrement() == 0) {
			String image = context.getConfigurationParameter(CONTAINER_IMAGE_PROPERTY).orElse(DEFAULT_CONTAINER_IMAGE);
			int clusterSize = context.getConfigurationParameter(INFINISPAN_CLUSTER_SIZE_PROPERTY, Integer::valueOf).orElse(this.defaultClusterSize);
			int port = context.getConfigurationParameter(INFINISPAN_SERVER_PORT_PROPERTY, Integer::valueOf).orElse(ConfigurationProperties.DEFAULT_HOTROD_PORT);
			boolean requireAuthentication = context.getConfigurationParameter(INFINISPAN_AUTHENTICATION_PROPERTY, Boolean::valueOf).orElse(DEFAULT_AUTHENTICATION);
			String username = context.getConfigurationParameter(INFINISPAN_USERNAME_PROPERTY).orElse(DEFAULT_HOTROD_USERNAME);
			String password = context.getConfigurationParameter(INFINISPAN_PASSWORD_PROPERTY).orElse(DEFAULT_HOTROD_PASSWORD);
			MountableFile configuration = context.getConfigurationParameter(INFINISPAN_CONFIGURATION_PROPERTY).map(MountableFile::forHostPath).orElse(null);
			try {
				for (int i = 0; i < clusterSize; ++i) {
					DefaultContainer container = new DefaultContainer(image);

					container.addExposedPort(port);
					container.setWaitStrategy(Wait.forLogMessage(".*\\QISPN080001\\E.*", 1));

					if (requireAuthentication) {
						container.addEnv(USERNAME_ENV, username);
						container.addEnv(PASSWORD_ENV, password);
					}
					if (configuration != null) {
						// Replace default configuration file within container
						container.withCopyFileToContainer(configuration, CONTAINER_IMAGE_CONFIGURATION_PATH);
					}

					container.start();

					this.containers.add(container);
				}
			} catch (RuntimeException | Error e) {
				while (!this.containers.isEmpty()) {
					Consumer.close().accept(this.containers.removeLast());
				}
				throw e;
			}
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (COUNTER.decrementAndGet() == 0) {
			while (!this.containers.isEmpty()) {
				Consumer.close().accept(this.containers.removeLast());
			}
		}
	}

	@Override
	public String toString() {
		return this.getURI().toString();
	}
}
