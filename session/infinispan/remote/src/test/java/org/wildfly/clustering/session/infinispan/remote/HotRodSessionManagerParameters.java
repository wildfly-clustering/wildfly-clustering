/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.remote;

import java.time.Duration;

import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.impl.HotRodURI;
import org.wildfly.clustering.session.cache.SessionManagerParameters;

/**
 * @author Paul Ferraro
 */
public interface HotRodSessionManagerParameters extends SessionManagerParameters {
	TransactionMode getTransactionMode();
	HotRodURI getHotRodURI();

	@Override
	default Duration getFailoverGracePeriod() {
		return (this.getTransactionMode() == TransactionMode.NONE) ? Duration.ofSeconds(1) : Duration.ZERO;
	}
}
