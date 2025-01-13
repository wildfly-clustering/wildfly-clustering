/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.function.Function;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.arquillian.AbstractITCase;
import org.wildfly.clustering.arquillian.Tester;

/**
 * Abstract container integration test.
 * @author Paul Ferraro
 */
public abstract class AbstractSessionManagerITCase extends AbstractITCase<SessionManagementTesterConfiguration, WebArchive> {

	@RegisterExtension
	static final ArquillianExtension ARQUILLIAN = new ArquillianExtension();

	protected AbstractSessionManagerITCase(SessionManagementTesterConfiguration configuration) {
		this(SessionManagementTester::new, configuration);
	}

	protected AbstractSessionManagerITCase(Function<SessionManagementTesterConfiguration, Tester> testerFactory, SessionManagementTesterConfiguration configuration) {
		super(testerFactory, configuration);
	}

	@Override
	public WebArchive createArchive(SessionManagementTesterConfiguration configuration) {
		return ShrinkWrap.create(WebArchive.class, this.getClass().getSimpleName() + ".war")
				.addClass(SessionManagementEndpointConfiguration.class)
				.addPackage(configuration.getEndpointClass().getPackage())
				;
	}
}
