/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.session.container;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

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
 * @param <C> the test configuration type
 * @param <A> the archive type
 */
public abstract class AbstractSessionManagerITCase<C, A extends Archive<A> & ClassContainer<A>> extends AbstractITCase<C, A> {

	@RegisterExtension
	static final ArquillianExtension ARQUILLIAN = new ArquillianExtension();

	private final SessionManagementTesterConfiguration configuration;
	private final Class<A> archiveClass;

	protected AbstractSessionManagerITCase(SessionManagementTesterConfiguration configuration, Class<A> archiveClass) {
		this(SessionManagementTester::new, configuration, archiveClass);
	}

	protected AbstractSessionManagerITCase(Function<SessionManagementTesterConfiguration, Tester> testerFactory, SessionManagementTesterConfiguration configuration, Class<A> archiveClass) {
		super(() -> testerFactory.apply(configuration));
		this.configuration = configuration;
		this.archiveClass = archiveClass;
	}

	@Override
	public A createArchive(C configuration) {
		String extension = ShrinkWrap.getDefaultDomain().getConfiguration().getExtensionLoader().getExtensionFromExtensionMapping(this.archiveClass);
		Class<?> endpointClass = this.configuration.getEndpointClass();
		Package endpointPackage = endpointClass.getPackage();
		A archive = ShrinkWrap.create(this.archiveClass, configuration.toString() + extension)
				.addClass(SessionManagementEndpointConfiguration.class)
				.addPackage(endpointPackage)
				;
		Optional.ofNullable(endpointClass.getSuperclass()).map(Class::getPackage).filter(Predicate.isEqual(endpointPackage).negate()).ifPresent(archive::addPackage);
		return archive;
	}
}
