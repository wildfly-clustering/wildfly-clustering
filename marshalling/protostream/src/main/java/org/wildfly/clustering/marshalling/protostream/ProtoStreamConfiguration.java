/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.marshalling.protostream;

import org.infinispan.protostream.config.Configuration;

/**
 * Extended ProtoStream configuration.
 * @author Paul Ferraro
 */
public interface ProtoStreamConfiguration extends Configuration {
	/**
	 * Returns the class loader of this configuration.
	 * @return the class loader of this configuration.
	 */
	ClassLoader getClassLoader();

	/**
	 * Builder of a ProtoStream configuration.
	 */
	interface Builder extends org.infinispan.protostream.config.Configuration.Builder {
		/**
		 * Returns a new configuration builder instance.
		 * @param loader the class loader of this configuration
		 * @return a new configuration builder instance.
		 */
		static Builder with(ClassLoader loader) {
			return new DefaultProtoStreamConfiguration.DefaultBuilder(loader);
		}
	}
}
