/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.arquillian;

/**
 * A closeable {@link DeploymentContainerRegistry}.
 * @author Paul Ferraro
 */
interface CloseableDeploymentContainerRegistry extends DeploymentContainerRegistry, AutoCloseable {
	@Override
	void close();
}
