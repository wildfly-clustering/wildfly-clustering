/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.user;

import java.util.function.Supplier;

import org.wildfly.clustering.session.user.User;

/**
 * Encapsulates a cacheable user.
 * @author Paul Ferraro
 * @param <C> the persistent context type
 * @param <T> the transient context type
 * @param <D> the deployment type
 * @param <S> the session type
 */
public interface CacheableUser<C, T, D, S> extends User<C, T, D, S>, Supplier<User<C, T, D, S>> {

}
