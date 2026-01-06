/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.Optional;
import java.util.function.Function;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.clustering.arquillian.AbstractITCase;
import org.wildfly.clustering.arquillian.Tester;

/**
 * Abstract container integration test.
 * @author Paul Ferraro
 * @param <A> the archive type
 */
public abstract class AbstractSessionManagerITCase<A extends Archive<A> & ClassContainer<A>> extends AbstractITCase<SessionManagementTesterConfiguration, A> {

	@RegisterExtension
	static final ArquillianExtension ARQUILLIAN = new ArquillianExtension();

	private final Class<A> archiveClass;

	protected AbstractSessionManagerITCase(SessionManagementTesterConfiguration configuration, Class<A> archiveClass) {
		this(SessionManagementTester::new, configuration, archiveClass);
	}

	protected AbstractSessionManagerITCase(Function<SessionManagementTesterConfiguration, Tester> testerFactory, SessionManagementTesterConfiguration configuration, Class<A> archiveClass) {
		super(testerFactory, configuration);
		this.archiveClass = archiveClass;
	}

	@Override
	public A createArchive(SessionManagementTesterConfiguration configuration) {
		String extension = ShrinkWrap.getDefaultDomain().getConfiguration().getExtensionLoader().getExtensionFromExtensionMapping(this.archiveClass);
		Class<?> endpointClass = configuration.getEndpointClass();
		Package endpointPackage = endpointClass.getPackage();
		A archive = ShrinkWrap.create(this.archiveClass, this.getClass().getSimpleName() + extension)
				.addClass(SessionManagementEndpointConfiguration.class)
				.addPackage(endpointPackage)
				;
		if (!Optional.ofNullable(endpointClass.getSuperclass()).map(Class::getPackage).orElse(endpointPackage).equals(endpointPackage)) {
			archive.addPackage(endpointClass.getSuperclass().getPackage());
		}
		return archive;
	}
}
