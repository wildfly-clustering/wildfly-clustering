/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.cache.infinispan.embedded.persistence;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.kohsuke.MetaInfServices;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(TwoWayKey2StringMapperTesterFactory.class)
public class IndexedKeyFormatMapperTesterFactory implements TwoWayKey2StringMapperTesterFactory {

	private final TwoWayKey2StringMapper mapper = IndexedKeyFormatMapper.load(Thread.currentThread().getContextClassLoader());

	@Override
	public TwoWayKey2StringMapper getMapper() {
		return this.mapper;
	}
}
