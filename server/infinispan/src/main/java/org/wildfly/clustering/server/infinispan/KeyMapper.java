/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.server.infinispan;

import org.infinispan.persistence.keymappers.TwoWayKey2StringMapper;
import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.cache.infinispan.embedded.persistence.LoadedKeyFormatMapper;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(TwoWayKey2StringMapper.class)
public class KeyMapper extends LoadedKeyFormatMapper {

	public KeyMapper() {
		super(KeyMapper.class.getClassLoader());
	}
}
