/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.server.provider;

import org.wildfly.clustering.server.GroupMember;
import org.wildfly.clustering.server.Registration;

/**
 * Encapsulates the registration of a provisioned service.
 *
 * @author Paul Ferraro
 * @param <T> the service type
 * @param <M> the member type
 */
public interface ServiceProviderRegistration<T, M extends GroupMember> extends ServiceProvision<T, M>, Registration {
}
