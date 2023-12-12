/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import org.wildfly.clustering.session.cache.Contextual;

/**
 * @author Paul Ferraro
 */
public interface UserContext<C, L> extends Contextual<L> {

	C getContext();
}
