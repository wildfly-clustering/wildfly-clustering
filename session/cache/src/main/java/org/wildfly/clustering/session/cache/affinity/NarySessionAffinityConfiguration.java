/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.affinity;

/**
 * @author Paul Ferraro
 */
public interface NarySessionAffinityConfiguration {

	String getDelimiter();

	int getMaxServers();
}
