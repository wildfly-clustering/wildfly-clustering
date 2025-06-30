/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

/**
 * An exception used to log the creation context of a batch.
 * @author Paul Ferraro
 */
public class ContextualException extends RuntimeException {
	private static final long serialVersionUID = -4254375328855589267L;

	ContextualException(Contextual contextual) {
		super(contextual.toString());
		contextual.attach(this);
	}
}
