/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Paul Ferraro
 */
public interface DeploymentContainer extends Lifecycle {

	Deployment deploy(Archive<?> archive);
}
