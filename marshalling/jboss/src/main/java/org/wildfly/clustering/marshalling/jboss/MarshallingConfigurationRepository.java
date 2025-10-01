/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.marshalling.jboss;

import java.io.IOException;
import java.util.EnumSet;
import java.util.function.Function;

import org.jboss.marshalling.MarshallingConfiguration;

/**
 * Repository of versioned {@link MarshallingConfiguration}s.
 * @author Paul Ferraro
 */
public interface MarshallingConfigurationRepository {
	/**
	 * Returns the current marshalling configuration version.
	 * @return a version
	 */
	int getCurrentVersion();

	/**
	 * Returns the marshalling configuration for the current version.
	 * @return a marshalling configuration
	 */
	MarshallingConfiguration getCurrentMarshallingConfiguration();

	/**
	 * Returns the marshalling configuration for the specified version.
	 * @param version a version
	 * @return a marshalling configuration
	 * @throws IOException if the specified version is unknown
	 */
	MarshallingConfiguration getMarshallingConfiguration(int version) throws IOException;

	/**
	 * Creates a marshalling configuration repository from on an enumeration of configuration factories.
	 * @param <C> the marshalling context
	 * @param <E> the enum type
	 * @param current the current version
	 * @param context the marshalling context
	 * @return a marshalling configuration repository
	 */
	static <C, E extends Enum<E> & Function<C, MarshallingConfiguration>> MarshallingConfigurationRepository from(E current, C context) {
		return from(current.ordinal() + 1, EnumSet.allOf(current.getDeclaringClass()).stream().map(c -> c.apply(context)).toArray(MarshallingConfiguration[]::new));
	}

	/**
	 * Creates a marshalling configuration repository from the specified ordered configurations.
	 * @param configurations a number of configurations
	 * @return a marshalling configuration repository
	 */
	static MarshallingConfigurationRepository from(MarshallingConfiguration... configurations) {
		return from(configurations.length, configurations);
	}

	/**
	 * Creates a marshalling configuration repository from on an enumeration of configuration factories.
	 * @param currentVersion the current version
	 * @param configurations a number of marshalling configurations
	 * @return a marshalling configuration repository
	 */
	static MarshallingConfigurationRepository from(int currentVersion, MarshallingConfiguration... configurations) {
		// First version is 1, not 0
		MarshallingConfiguration currentConfiguration = configurations[currentVersion - 1];
		return new MarshallingConfigurationRepository() {
			@Override
			public int getCurrentVersion() {
				return currentVersion;
			}

			@Override
			public MarshallingConfiguration getCurrentMarshallingConfiguration() {
				return currentConfiguration;
			}

			@Override
			public MarshallingConfiguration getMarshallingConfiguration(int version) throws IOException {
				if ((version <= 0) || (version > configurations.length)) {
					throw new IOException(new IllegalArgumentException(Integer.toString(version)));
				}
				return configurations[version - 1];
			}
		};
	}
}
