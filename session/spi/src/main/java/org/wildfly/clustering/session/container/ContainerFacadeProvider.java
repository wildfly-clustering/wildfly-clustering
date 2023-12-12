/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

/**
 * Provides container-specific facades to a session manager implementation.
 * @author Paul Ferraro
 * @param <S> the container-specific session facade type
 * @param <C> the container-specific session manager context type
 * @param <L> the container-specific activation listener type
 */
public interface ContainerFacadeProvider<S, C, L> extends SessionActivationListenerFacadeProvider<S, C, L> {
}
