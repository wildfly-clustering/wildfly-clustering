/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.arquillian;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.testcontainers.containers.Network;

/**
 * Provides a container network as an Arquillian resource.
 * @author Paul Ferraro
 */
public class NetworkResourceProvider implements ResourceProvider {

	@Inject
	private Instance<Network> network;

	/** Default constructor */
	public NetworkResourceProvider() {
		// Do nothing
	}

	@Override
	public boolean canProvide(Class<?> type) {
		return type.isAssignableFrom(Network.class);
	}

	@Override
	public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
		return this.network.get();
	}
}
