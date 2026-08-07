/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.wildfly.clustering.function.UnaryOperator;

/**
 * An OCI container configured from a set of properties.
 * @author Paul Ferraro
 */
public class DefaultContainer extends GenericContainer<DefaultContainer> {

	static final String IMAGE_PROPERTY = "oci:image";
	static final String NETWORK_MODE_PROPERTY = "oci:network-mode";
	static final String START_TIMEOUT_PROPERTY = "oci:start-timeout";
	static final String START_MESSAGE_PATTERN_PROPERTY = "oci:start-message-pattern";
	static final String ENV_PROPERTY_PREFIX = "env:";
	static final String REF_PROPERTY_PREFIX = "ref:";
	static final String FILE_PROPERTY_PREFIX = "file:";

	private static final System.Logger LOGGER = System.getLogger(DefaultContainer.class.getName());

	private static final String DEFAULT_NETWORK_MODE = "bridge";
	private static final Duration DEFAULT_START_TIMEOUT = Duration.ofMinutes(1L);

	private final Duration startTimeout;

	/**
	 * Create a generic test container.
	 * @param image the container image name
	 */
	public DefaultContainer(String image) {
		this(image, DEFAULT_START_TIMEOUT);

		this.setNetworkMode(DEFAULT_NETWORK_MODE);
}

	/**
	 * Create a generic test container.
	 * @param properties the properties with which to configure the OCI container
	 * @param references a property reference resolver
	 */
	public DefaultContainer(Map<String, String> properties, UnaryOperator<String> references) {
		this(Objects.requireNonNull(properties.get(IMAGE_PROPERTY)), Optional.ofNullable(properties.get(START_TIMEOUT_PROPERTY)).map(Duration::parse).orElse(DEFAULT_START_TIMEOUT));

		this.setNetworkMode(properties.getOrDefault(NETWORK_MODE_PROPERTY, DEFAULT_NETWORK_MODE));

		List<Map.Entry<String, String>> paths = new ArrayList<>(properties.size());
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.startsWith(ENV_PROPERTY_PREFIX)) {
				this.addEnv(key.substring(ENV_PROPERTY_PREFIX.length()), value);
			} else if (key.startsWith(REF_PROPERTY_PREFIX)) {
				String ref = references.apply(value);
				if (ref == null) {
					throw new IllegalArgumentException(value);
				}
				this.addEnv(key.substring(REF_PROPERTY_PREFIX.length()), ref);
			} else if (key.startsWith(FILE_PROPERTY_PREFIX)) {
				paths.add(Map.entry(key.substring(FILE_PROPERTY_PREFIX.length()), value));
			}
		}

		for (Map.Entry<String, String> entry : paths) {
			this.withCopyFileToContainer(MountableFile.forHostPath(entry.getKey()), entry.getValue());
		}

		if (properties.containsKey(START_MESSAGE_PATTERN_PROPERTY)) {
			this.setWaitStrategy(Wait.forLogMessage(properties.get(START_MESSAGE_PATTERN_PROPERTY), 1));
		}
	}

	private DefaultContainer(String image, Duration startTimeout) {
		super(DockerImageName.parse(image));
		this.startTimeout = startTimeout;

		this.setHostAccessible(true);
		this.setNetworkMode(DEFAULT_NETWORK_MODE);

		Map<OutputFrame.OutputType, Optional<PrintStream>> outputs = new EnumMap<>(OutputFrame.OutputType.class);
		outputs.put(OutputFrame.OutputType.END, Optional.empty());
		outputs.put(OutputFrame.OutputType.STDERR, Optional.of(System.err));
		outputs.put(OutputFrame.OutputType.STDOUT, Optional.of(System.out));

		this.withLogConsumer(frame -> outputs.get(frame.getType()).ifPresent(stream -> stream.println(frame.getUtf8StringWithoutLineEnding())));

		this.setWaitStrategy(Wait.defaultWaitStrategy());
	}

	@Override
	public void setWaitStrategy(WaitStrategy waitStrategy) {
		super.setWaitStrategy(waitStrategy.withStartupTimeout(this.startTimeout));
	}

	@Override
	public void start() {
		LOGGER.log(System.Logger.Level.INFO, "Starting {0}", this);
		Instant start = Instant.now();
		super.start();
		LOGGER.log(System.Logger.Level.INFO, "Started {0} in {1}", this, Duration.between(start, Instant.now()));
	}

	@Override
	public void stop() {
		LOGGER.log(System.Logger.Level.INFO, "Stopping {0}", this);
		Instant start = Instant.now();
		super.stop();
		LOGGER.log(System.Logger.Level.INFO, "Stopped {0} in {1}", this, Duration.between(start, Instant.now()));
	}

	@Override
	public boolean equals(Object object) {
		return (object instanceof GenericContainer container) && this.getImage().equals(container.getImage());
	}

	@Override
	public int hashCode() {
		return this.getImage().hashCode();
	}

	@Override
	public String toString() {
		return this.getImage().toString();
	}
}
