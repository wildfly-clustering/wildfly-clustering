/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.user;

import java.util.function.Supplier;

import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.server.manager.ManagerConfiguration;

/**
 * Encapsulates the configuration of a user manager.
 * @author Paul Ferraro
 * @param <T> the transient context type
 * @param <B> the batch type
 */
public interface UserManagerConfiguration<T, B extends Batch> extends ManagerConfiguration<String> {
	ByteBufferMarshaller getMarshaller();
	Supplier<T> getTransientContextFactory();
}
