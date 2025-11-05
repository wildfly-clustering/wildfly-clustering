/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container.servlet;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.ImmutableSessionMetaData;

/**
 * Unit test for {@link ImmutableHttpSession}.
 * @author Paul Ferraro
 */
public class ImmutableHttpSessionTestCase extends AbstractHttpSessionTestCase<ImmutableSession, ImmutableSessionMetaData> {

	public ImmutableHttpSessionTestCase() {
		super(ImmutableSession.class, ImmutableSessionMetaData.class, ImmutableHttpSession::new);
	}
}
