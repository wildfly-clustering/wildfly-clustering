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
@MetaInfServices(FormatterTesterFactory.class)
public class KeyFormatterMapperTesterFactory implements FormatterTesterFactory {

	private final TwoWayKey2StringMapper mapper = KeyFormatterMapper.load(Thread.currentThread().getContextClassLoader());

	@Override
	public TwoWayKey2StringMapper getMapper() {
		return this.mapper;
	}
}
