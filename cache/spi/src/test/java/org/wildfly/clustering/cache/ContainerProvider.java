/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache;

import org.junit.jupiter.api.extension.Extension;
import org.testcontainers.containers.Container;

/**
 * JUnit extension providing a container.
 * @param <C> the container type
 * @author Paul Ferraro
 */
public interface ContainerProvider<C extends Container<C>> extends Extension {
	C getContainer();
}
