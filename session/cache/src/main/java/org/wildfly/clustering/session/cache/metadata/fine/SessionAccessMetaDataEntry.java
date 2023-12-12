/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import org.wildfly.clustering.cache.function.Remappable;

/**
 * @author Paul Ferraro
 */
public interface SessionAccessMetaDataEntry extends SessionAccessMetaData, Remappable<SessionAccessMetaDataEntry, SessionAccessMetaDataEntryOffsets> {

}
