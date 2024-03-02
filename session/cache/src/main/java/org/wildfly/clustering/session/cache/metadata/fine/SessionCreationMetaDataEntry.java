/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.cache.metadata.fine;

import java.time.Duration;
import java.util.function.Supplier;

import org.wildfly.clustering.cache.function.Remappable;
import org.wildfly.clustering.server.offset.Offset;
import org.wildfly.clustering.session.cache.Contextual;

/**
 * A contextual session metadata entry.
 * @author Paul Ferraro
 * @param <C> the session context type
 */
public interface SessionCreationMetaDataEntry<C> extends SessionCreationMetaData, Contextual<C>, Remappable<SessionCreationMetaDataEntry<C>, Supplier<Offset<Duration>>> {
}
