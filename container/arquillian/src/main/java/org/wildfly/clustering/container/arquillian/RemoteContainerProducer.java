/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.arquillian;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStopping;
import org.testcontainers.containers.Network;
import org.wildfly.clustering.arquillian.LifecycleFactory;
import org.wildfly.clustering.function.Consumer;

/**
 * Produces a container network and lifecycle factory.
 * @author Paul Ferraro
 */
public class RemoteContainerProducer {

	@ApplicationScoped
	@Inject
	private InstanceProducer<Network> network;

	@ApplicationScoped
	@Inject
	private InstanceProducer<LifecycleFactory> lifecycleFactory;

	/** Default constructor required by Arquillian */
	public RemoteContainerProducer() {
		// Do nothing
	}

	/**
	 * Observes {@link ArquillianDescriptor} availability.
	 * @param event the observed event
	 */
	public void init(@SuppressWarnings("unused") @Observes(precedence = Integer.MAX_VALUE) ArquillianDescriptor event) {
		this.network.set(Network.newNetwork());
		this.lifecycleFactory.set(new RemoteContainerLifecycleFactory(this.network.get()));
	}

	/**
	 * Observes {@link ManagerStopping} events.
	 * @param event an event
	 */
	public void destroy(@SuppressWarnings("unused") @Observes(precedence = Integer.MIN_VALUE) ManagerStopping event) {
		Consumer.close().accept(this.network.get());
	}
}
