/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.container.arquillian;

import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.kohsuke.MetaInfServices;

/**
 * An arquillian extension providing remote container support.
 * @author Paul Ferraro
 */
@MetaInfServices(LoadableExtension.class)
public class RemoteContainerExtension implements LoadableExtension {

	/** Default constructor */
	public RemoteContainerExtension() {
		// Do nothing
	}

	@Override
	public void register(ExtensionBuilder builder) {
		builder.observer(RemoteContainerProducer.class);
		builder.service(ResourceProvider.class, NetworkResourceProvider.class);
	}
}
