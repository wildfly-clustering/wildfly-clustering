/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.infinispan.embedded.metadata;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Formatter;
import org.wildfly.clustering.session.infinispan.embedded.SessionKeyFormatter;

/**
 * Formatter for a {@link SessionMetaDataKey}.
 * @author Paul Ferraro
 */
@MetaInfServices(Formatter.class)
public class SessionMetaDataKeyFormatter extends SessionKeyFormatter<SessionMetaDataKey> {

	public SessionMetaDataKeyFormatter() {
		super(SessionMetaDataKey::new);
	}
}
