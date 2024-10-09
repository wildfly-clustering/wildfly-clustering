/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.dispatcher;

/**
 * A command that does not throw checked exceptions.
 *
 * @param <C> the command context type
 * @param <R> the command return type
 * @author Paul Ferraro
 */
public interface RuntimeCommand<R, C> extends Command<R, C, RuntimeException> {
	@Override
	R execute(C context);
}
