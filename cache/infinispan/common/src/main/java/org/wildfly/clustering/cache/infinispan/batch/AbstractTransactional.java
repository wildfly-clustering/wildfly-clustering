/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.batch;

import java.util.Map;

/**
 * @author Paul Ferraro
 */
public abstract class AbstractTransactional implements Transactional {

	@Override
	public int hashCode() {
		return this.getTransaction().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Transactional)) return false;
		Transactional batch = (Transactional) object;
		return this.getTransaction().equals(batch.getTransaction());
	}

	@Override
	public String toString() {
		return Map.of("context", this.getContext(), "tx", this.getTransaction()).toString();
	}
}
