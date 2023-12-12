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
 * @author Paul Ferraro
 * @param <L> local context type
 */
public interface UserManagerConfiguration<L, B extends Batch> extends ManagerConfiguration<String, B> {
	ByteBufferMarshaller getMarshaller();
	Supplier<L> getUserContextFactory();
}
