/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata;

import org.wildfly.clustering.cache.Locator;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * @author Paul Ferraro
 */
public interface ImmutableSessionMetaDataFactory<V> extends Locator<String, V> {
	ImmutableSessionMetaData createImmutableSessionMetaData(String id, V value);
}
